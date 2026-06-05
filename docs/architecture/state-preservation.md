# State Preservation

How solivagant and kmp-viewmodel handle state restoration across Android configuration changes,
process death, and non-Android platform lifecycle events.

---

## 1. The Problem Space

State preservation in a multiplatform app has multiple distinct layers:

| Layer | Android mechanism | Non-Android equivalent |
|-------|------------------|----------------------|
| ViewModel cached state (config changes only) | `ViewModel` retained by `ViewModelStore` | `ViewModel` retained by `ViewModelStore` (custom impl) |
| User-entered state (config changes + process death) | `SavedStateHandle` (Parcel-based) | Custom serialization via `rememberSaveable` |
| Back stack structure | `NavBackStack` serialized via Parcel | `rememberSaveable` JSON/custom serialization |
| Compose UI state within a screen | `rememberSaveable` | `rememberSaveable` |

A navigation library must coordinate all four layers correctly. Getting any one wrong causes either
lost state (bad UX) or incorrect lifecycle management (memory leaks, crashes).

---

## 2. Android: The Standard Path

On Android, solivagant relies on standard Android mechanisms:

- The navigation back stack (`MultiStackSavedState`) is saved via `rememberSaveable`. Since
  `MultiStackSavedState` is `Parcelable`, it survives both configuration changes and process death.
- `ViewModel` instances are retained across configuration changes via the `ViewModelStore` owned
  by each `StackEntry`. When the `NavHost` is recreated after a configuration change, it restores
  the `MultiStack` from saved state and reconnects to existing `StackEntry` ViewModelStores.
- `SavedStateHandle` for each stack entry is created via `executor.savedStateHandleFor(entry.id)`,
  which creates a `SavedStateHandle` backed by Android's saved instance state mechanism.

This means Android behavior is correct by construction: the same mechanisms Android uses for
Activity/Fragment state management apply to each navigation entry.

---

## 3. Non-Android: SavedStateSupport

On non-Android platforms (Desktop, iOS, Web), there is no system-provided saved instance state.
solivagant provides `SavedStateSupport` to fill this gap.

### What SavedStateSupport does

`SavedStateSupport` is a single class that implements three interfaces:

```kotlin
class SavedStateSupport :
    SaveableStateRegistry,      // Compose's state-saving mechanism
    SavedStateHandleFactory,    // Creates SavedStateHandle objects
    ViewModelStoreOwner         // Holds ViewModelStore for root-level ViewModels
```

By implementing `SaveableStateRegistry`, `SavedStateSupport` can collect values registered
by `rememberSaveable` composables within its scope. The collected state can be serialized and
passed to the next composition (e.g., when the app is reconstructed).

### Platform integration examples

#### Desktop (JVM)

```kotlin
val savedStateSupport = SavedStateSupport()

application {
    Window(onCloseRequest = ::exitApplication) {
        // Restores rememberSaveable state across window recompositions
        savedStateSupport.ProvideCompositionLocals {
            MyApp()
        }
    }
    
    // Clear ViewModel stores when the window closes
    savedStateSupport.ClearOnDispose()
}
```

On Desktop, `SavedStateSupport` primarily helps with in-process recomposition. It does not
persist state across app restarts (no disk serialization is provided by the library).

#### iOS

```swift
// Swift-side: SavedStateSupport is owned by a SwiftUI ViewModel
class ComposeViewViewModel: ObservableObject {
    let savedStateSupport = NavigationSavedStateSupport()
    
    deinit {
        // Clear ViewModels when the SwiftUI ViewModel is deallocated
        self.savedStateSupport.clear()
    }
}
```

```kotlin
// Kotlin-side: SavedStateSupport is passed to the Compose UI
fun MainViewController(savedStateSupport: SavedStateSupport): UIViewController {
    return ComposeUIViewController {
        LifecycleOwnerProvider(…) {
            savedStateSupport.ProvideCompositionLocals {
                MyApp()
            }
        }
    }
}
```

The `SavedStateSupport` is owned by the SwiftUI ViewModel, which has a lifecycle tied to the
SwiftUI view hierarchy. This means ViewModels are cleared when the SwiftUI view is deallocated.

### Limitations of SavedStateSupport on non-Android platforms

1. **No serialization to disk is built in.** The navigation back stack survives in-process
   recompositions but not app restarts on Desktop/iOS unless the app code serializes
   `savedStateSupport.performSave()` to disk manually.

2. **The SavedStateHandle returned on non-Android is in-memory only.** Values stored in it do
   not survive process restart. For Android this is fine (the Android implementation uses
   real Parcels); for iOS/Desktop this means `SavedStateHandle` is only useful for
   within-session state restoration.

3. **The platform integration glue is non-trivial.** Correctly integrating `SavedStateSupport`
   with the platform lifecycle (especially iOS UIViewController lifecycle) requires careful
   reading of the samples.

---

## 4. Per-Entry State Isolation

Each navigation stack entry has isolated state at three levels:

### 4.1 Compose saveable state

The `NavHost` wraps each destination in `SaveableStateHolder.SaveableStateProvider(entry.id.value)`.
This scopes all `rememberSaveable` calls within the destination to the entry's ID. When the entry
is popped, `saveableStateHolder.removeState(entry.id.value)` clears its saved state.

This means:
- Two instances of the same destination on the back stack have independent `rememberSaveable` state.
- State is not leaked when a destination is removed.

### 4.2 ViewModelStore per entry

Each `StackEntry` owns a `StackEntryViewModelStoreOwner` backed by a fresh `ViewModelStore`.
When the entry is removed, its `ViewModelStore` is cleared (all ViewModels in it receive
`onCleared()`). ViewModels do not outlive their navigation entry.

### 4.3 SavedStateHandle per entry

Each `StackEntry`'s `SavedStateHandle` is keyed by the entry's UUID (`entry.id.value`). On
Android, this creates a separate `SavedStateHandle` backed by the navigation's restored bundle.
On non-Android, it creates a separate in-memory map per entry.

---

## 5. Back Stack Serialization

`MultiStackSavedState` captures the entire multi-backstack state:

```kotlin
@Parcelize
class MultiStackSavedState(
    val allStackSavedStates: ArrayList<StackSavedState>,
    val currentStackId: DestinationId<*>,
    val startDestinationId: DestinationId<*>,
) : Parcelable
```

Each `StackSavedState` stores the ordered list of routes on that stack. Because routes are
`Parcelable` data classes, they serialize completely without extra configuration.

`MultiStack.fromState(savedState, destinations, …)` reconstructs the `MultiStack` by:
1. Recreating `Stack` instances from saved route lists.
2. Matching each route to its `NavDestination` by type (using `DestinationId`).
3. Restoring `currentStack` and `startStack` pointers.

The destinations (UI composables) are not serialized — they are always registered at app startup
and matched to routes at restore time. This is the same design as AndroidX Navigation: the graph
is registered in code, the back stack state is saved.

---

## 6. StackEntry Lifecycle and State Correctness

The lifecycle of a `StackEntry` determines when associated resources are valid:

```
INITIALIZED → CREATED → STARTED → RESUMED
                                   ↓
                            (STARTED during exit transition)
                                   ↓
                            CREATED → DESTROYED
```

This lifecycle is critical for correct state behaviour:

- **`STARTED`** (visible but transitioning): `rememberSaveable` and Compose state are active,
  but flows collected `collectAsStateWithLifecycle(minActiveState = STARTED)` emit.
- **`RESUMED`** (fully visible, transition complete): All observers receive updates.
- **`CREATED`** (off-screen): Overlay is not in background; screen entry in a background stack.
- **`DESTROYED`**: State is cleared. `ViewModelStore.clear()` is called.

Advancing to `RESUMED` only after the AnimatedContent transition completes prevents observers
from firing during the screen-enter animation, which could cause visual glitches from data
updates during transition.

---

## 7. Design Trade-offs

| Concern | solivagant approach | Official Navigation Compose | Navigation 3 |
|---------|--------------------|-----------------------------|--------------|
| Route serialization | `@Parcelize` (Android Parcel) | `@Serializable` + deep links | `@Serializable` |
| Back stack persistence (Android) | `rememberSaveable` + `Parcelable` | `NavController` built-in | App code with `rememberSaveable` |
| Back stack persistence (non-Android) | Manual: `SavedStateSupport.performSave()` | N/A | App code |
| ViewModel store per entry | ✅ Yes | ✅ Yes | ⚠️ Uncertain |
| `SavedStateHandle` per entry | ✅ Yes (Android full; non-Android in-memory) | ✅ Yes (Android) | ⚠️ Uncertain |
| State isolation between entries | ✅ Yes (UUID keyed `SaveableStateHolder`) | ✅ Yes | ⚠️ Uncertain |
