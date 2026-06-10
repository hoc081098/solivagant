# Extracted Patterns

A catalogue of valuable ideas, patterns, and coding techniques from solivagant and kmp-viewmodel,
grouped by theme.

---

## Navigation Stack Modeling

### Pattern: Back Stack as Observable Compose State

**Source:** `solivagant/navigation/src/commonMain/kotlin/…/internal/MultiStack.kt`

The entire multi-backstack is modeled as a Kotlin class with methods for state transitions. The
visible entries are exposed as `State<VisibleEntryState>` (Compose state). The `NavHost`
composable reads this state and recomposes when it changes. There is no direct mutation of the
back stack from the UI — all mutations happen through the `MultiStack` methods, which are called
by the `MultiStackNavigationExecutor` in response to collected `NavEvent` objects.

This is the Compose-idiomatic version of the "back stack as value" idea that Navigation 3
independently arrived at.

**Still relevant:** Yes. The pattern of expressing navigation state as observable Compose state
is now endorsed by Google/JetBrains.

**Blog potential:** "How we modeled navigation state before Navigation 3 had it."

---

### Pattern: Multi-Backstack Ownership via NavRoot

**Source:** `NavRoute.kt`, `MultiStack.kt`

`NavRoot` is a separate type from `NavRoute`. A `MultiStack` holds one `Stack` per `NavRoot`.
Switching `NavRoot` saves and restores the associated `Stack`. The `startStack` is the home
`NavRoot`'s stack; navigating back from any other root's stack navigates back to the `startStack`.

This two-level hierarchy (`Root → Stack → Entries`) cleanly models the real structure of apps
with bottom navigation:

```
App
├── HomeRoot         [ HomeRoot, ProductList, ProductDetail ]
├── SearchRoot       [ SearchRoot, SearchResults ]
└── ProfileRoot      [ ProfileRoot ]
```

**Still relevant:** Yes. The explicit distinction between "tab root" and "entry within a tab" is
a useful mental model regardless of which navigation library is used.

**Migration note:** Navigation 3 does not have this concept natively yet. If your app uses
multi-backstacks, this is a reason to keep solivagant or implement the pattern manually.

---

### Pattern: Navigation Commands as a Channel

**Source:** `NavEventNavigator.kt`

Navigation intent is expressed as `NavEvent` objects sent to a `Channel.UNLIMITED`. The channel
is collected by the `NavHost` and applied to the `MultiStack`.

Benefits:
- ViewModels do not need Compose context.
- Navigation is testable: assert events emitted, not state changes.
- Events are buffered: navigation commands emitted before the NavHost attaches are not dropped.
- Multiple commands can be batched: `navigator.navigate { navigateBackTo<HomeRoute>(); navigateTo(DetailRoute(id)) }`.

**Still relevant:** Yes. The pattern of using `Channel`/`SharedFlow` for ViewModel-to-UI
communication is widely applicable.

**Template:**
```kotlin
// Generic version of the NavEventNavigator pattern
class CommandEmitter<C> {
    private val _commands = Channel<C>(Channel.UNLIMITED)
    val commands: Flow<C> = _commands.receiveAsFlow()

    fun emit(command: C) {
        _commands.trySend(command)
    }
}
```

---

## Type-Safe Routes

### Pattern: Routes as Data Classes with Parcelize

**Source:** `NavRoute.kt`, kmp-viewmodel `@Parcelize`

Navigation arguments are encoded as properties of a `Parcelable` data class. No bundle
construction, no argument parsing, no string keys.

```kotlin
@Parcelize
data class ProductDetailRoute(val id: String, val fromSearch: Boolean = false) : NavRoute
```

The route arrives at the destination as a typed object accessible directly.

**Still relevant:** Yes. Navigation 3 uses `@Serializable` classes with the same intent.
The mechanism (Parcelable vs. Serializable) differs; the principle is the same.

**Key insight:** Treating a navigation destination as a function call (arguments → destination)
and encoding that as a data class makes the API refactor-friendly. Renaming arguments is a
compiler-checked refactor, not a string search.

---

### Pattern: Destination Registration Without Reflection

**Source:** `NavDestination.kt`, `NavHost.kt`

Destinations are registered as a `ImmutableSet<NavDestination>` passed to `NavHost`. Each
`NavDestination` is a value object binding a route type (via `DestinationId`) to a composable.

```kotlin
val AllDestinations: ImmutableSet<NavDestination> = persistentSetOf(
    ScreenDestination<HomeRoute> { route, modifier -> HomeScreen(modifier) },
    ScreenDestination<DetailRoute> { route, modifier -> DetailScreen(modifier, route) },
)
```

Route-to-destination matching is done by type, not by string name. The set is built at compile
time and is fully inspectable.

**Still relevant:** Yes. This avoids the reflection-based route registration in older Navigation
Compose DSL. Navigation 3 uses a similar approach with sealed interfaces or when-expressions.

---

## ViewModel Lifecycle

### Pattern: expect/actual ViewModel Bridging

**Source:** `kmp-viewmodel/viewmodel/src/commonMain/kotlin/…/ViewModel.kt`

The `ViewModel` class is declared as `expect` in `commonMain` and `actual`-implemented per target.
On Android, the `actual` is a subclass or typealias of `androidx.lifecycle.ViewModel`. On other
targets, it is a pure Kotlin class with a `viewModelScope` cancellable via `onCleared()`.

This pattern provides 100% API compatibility with AndroidX ViewModel on Android while keeping
the interface usable from common code.

**Still relevant as a technique:** This is the canonical approach for bridging Android-only APIs
to KMP. The specific ViewModel bridge is now provided by JetBrains, but the pattern (declare
interface in common, delegate to platform-native in `actual`) is broadly applicable.

---

### Pattern: Closeable Resources Tied to ViewModel Lifetime

**Source:** `ViewModel.kt` in kmp-viewmodel

```kotlin
class MyViewModel(
    private val subscription: MyCloseable,
) : ViewModel(subscription) {
    // subscription is closed when ViewModel.onCleared() is called
}

// Or dynamically:
viewModel.addCloseable(myResource)
```

Resources are registered with the ViewModel and automatically closed when the ViewModel is
cleared. No need to override `onCleared()` for each resource.

**Still relevant:** Yes. This is now part of AndroidX ViewModel and JetBrains lifecycle
multiplatform. The pattern of RAII-style resource scoping via `Closeable` is valuable in any
lifecycle-aware context.

---

## State Preservation

### Pattern: Type-Safe SavedStateHandle Keys

**Source:** `kmp-viewmodel-savedstate`, `NonNullSavedStateHandleKey.kt`

```kotlin
private val searchTermKey = NonNullSavedStateHandleKey.string("search_term", defaultValue = "")

// Type-safe access — no casting
val term: String = savedStateHandle.safe[searchTermKey]
val flow: StateFlow<String> = savedStateHandle.safe.getStateFlow(searchTermKey)
```

The key object encodes both the string key and the value type. The `safe` accessor enforces that
access matches the declared type.

**Still relevant:** Yes. No official equivalent exists. This pattern eliminates a class of
runtime errors (wrong type accessed from `SavedStateHandle`) at compile time.

**Worth extracting:** Yes. The key classes are small (~100 lines) and can be extracted into
any project independently. Or kept as a dependency on `kmp-viewmodel-savedstate`.

---

### Pattern: Per-Entry State Isolation in NavHost

**Source:** `NavHost.kt`, `SaveableCloseable` inner class

Each navigation entry is wrapped in:
```kotlin
saveableStateHolder.SaveableStateProvider(entry.id.value) { … }
```

This keys all `rememberSaveable` state within the entry to the entry's UUID. When the entry
is removed from the stack, `saveableStateHolder.removeState(entry.id.value)` clears it.

**Still relevant:** Yes. Any custom navigation host implementation should implement this pattern
to prevent state leaks when destinations are removed.

---

### Pattern: SavedStateSupport as a Cross-Platform State Registry

**Source:** `SavedStateSupport.kt` in `solivagant/navigation`

`SavedStateSupport` implements `SaveableStateRegistry`, `SavedStateHandleFactory`, and
`ViewModelStoreOwner` in a single class, enabling the full Compose state-saving machinery on
non-Android platforms by wrapping the root composable:

```kotlin
savedStateSupport.ProvideCompositionLocals { MyApp() }
```

**Still relevant as a pattern:** Yes. Any app targeting non-Android platforms with Compose needs
a similar bridge. The implementation is a useful reference.

---

## Compose Integration

### Pattern: CompositionLocal Injection of Navigation Infrastructure

**Source:** `LocalLifecycleOwner.kt`, `LocalNavigationExecutor` in `NavHost.kt`

Rather than passing navigation objects down through composable parameters, they are provided as
`CompositionLocal` values and consumed via `LocalX.current`. This follows the Compose convention
for framework-level dependencies that do not change often but need to be accessible deep in the
tree.

**Still relevant:** Yes. This is the standard Compose pattern for injecting stable infrastructure.

---

### Pattern: Lifecycle State Controls AnimatedContent Target State

**Source:** `NavHost.kt` — the `transition.AnimatedContent` block

The `AnimatedContent` transition is driven by `VisibleEntryState`, which includes `lastEvent`
(Push, Pop, PushRoot, etc.). Transition animations are selected based on the event type, not the
current/target route. This means the animation responds to navigation intent, not route identity.

After the transition completes, the entry's lifecycle is advanced to `RESUMED`. This prevents
data updates (from `collectAsStateWithLifecycle`) from triggering recompositions during the
enter animation.

**Still relevant:** Yes. Synchronizing lifecycle state with animation completion is an important
correctness detail for navigation animations in Compose.

---

## Swift Interop

### Pattern: Typed Flow Wrappers for Swift

**Source:** `kmp-viewmodel/viewmodel/src/commonMain/kotlin/…/wrapper/`

```kotlin
class NonNullStateFlowWrapper<T : Any>(private val flow: StateFlow<T>) {
    val value: T get() = flow.value
    
    fun subscribe(scope: CoroutineScope, onValue: (T) -> Unit): Closeable {
        val job = scope.launch { flow.collect { onValue(it) } }
        return Closeable { job.cancel() }
    }
}
```

The wrapper provides a `subscribe(scope:onValue:)` method that is callable from Swift without
casting. Swift can use the `value` property with the correct type.

**Still relevant:** For projects using Kotlin < 2.0 or specific Swift/ObjC targets, yes.
For Kotlin 2.0+ with modern Swift, verify whether direct `StateFlow` consumption works.

---

## API Design

### Pattern: InternalNavigationApi Annotation for Sealed Leakage Prevention

**Source:** `InternalNavigationApi.kt`

```kotlin
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal navigation API that should not be used from outside the library."
)
annotation class InternalNavigationApi
```

Internal extension points (e.g., `NavigationExecutor`, `ContentDestination.content`) are
annotated `@InternalNavigationApi`. Users can opt in but are warned they are using unstable
internals. This keeps the public API surface small while allowing testing of internals.

**Still relevant:** Yes. This pattern (`@RequiresOptIn` for internal-but-accessible APIs) is
standard Kotlin library design.

---

### Pattern: Warning / Lenient / Strict Validation Modes

**Source:** `StackValidationMode.kt`

Rather than having a single validation policy for the back stack, solivagant exposes three
modes: `Strict` (throw), `Lenient` (silently ignore), `Warning` (log). The default is `Lenient`
for production; `Warning.Debug` for debug builds.

This separates correctness enforcement from crash policy. A team can enforce strict validation
in tests without crashing users if the stack enters an unexpected state in production.

**Still relevant:** Yes. This pattern applies to any invariant-guarded subsystem where
"crash on invariant violation" is not always appropriate.

---

## Documentation and Developer Experience

### Pattern: README Samples That Match Real Usage

Both libraries include complete, runnable sample apps (not snippets) that show the library
integrated with real DI containers (Koin, Koject), on real platforms (Android, Desktop, iOS),
with real architecture (ViewModel, UseCase, Repository). This is more valuable than synthetic
"hello world" examples.

**Still relevant:** Yes. Sample apps are high-value documentation that ages better than prose.

---

## Summary

| Pattern | Unique | Still Relevant | Worth Extracting |
|---------|--------|----------------|-----------------|
| Back stack as observable Compose state | No (now standard) | ✅ | No |
| NavRoot multi-backstack ownership | Yes | ✅ | Yes |
| Navigation commands via Channel | Yes | ✅ | ✅ Yes |
| Typed route data classes | No | ✅ | No (standard now) |
| expect/actual ViewModel bridging | No | ✅ technique | Yes (as pattern doc) |
| Closeable chain on ViewModel | No | ✅ | No (now standard) |
| Type-safe SavedStateHandle keys | **Yes** | ✅ | **✅ Yes** |
| Per-entry state isolation in NavHost | Partial | ✅ | Yes |
| SavedStateSupport cross-platform | Yes | ✅ | Yes |
| Swift Flow wrappers | Yes | ⚠️ Partial | ✅ Yes |
| `@InternalNavigationApi` opt-in | No | ✅ | Yes (API design) |
| Validation mode pattern | Yes | ✅ | ✅ Yes |
