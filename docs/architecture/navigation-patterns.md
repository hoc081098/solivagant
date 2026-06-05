# Navigation Patterns

Patterns from solivagant that are worth preserving as architecture references.

---

## 1. Routes as First-Class Typed Values

### What it is

Navigation destinations are described as Kotlin data classes or objects, not as string URLs or
integer IDs. Arguments are fields of the route class. The entire route is `Parcelable` so it can
survive Android process death and be restored.

```kotlin
@Immutable
@Parcelize
data class ProductDetailRoute(
    val productId: String,
    val fromSearch: Boolean = false,
) : NavRoute

@Immutable
@Parcelize
data object HomeRoute : NavRoot
```

### Why it was interesting

- Navigation errors are compile-time errors. Passing the wrong type to a destination is impossible.
- No argument parsing code. The route arrives at the destination as a fully typed object.
- Routes can be unit-tested, copied, compared with `==`, and logged.
- Because `NavRoute` is `Parcelable`, the entire back stack can be serialized without extra
  serialization configuration.

### Current relevance

This pattern is directly relevant today. Navigation 3 uses `@Serializable` classes as routes with
the same intent. The specific mechanism (Parcelable vs. Kotlin Serialization) differs, but the
design principle — routes as typed value objects — is now mainstream.

**Navigation 3 equivalent:**
```kotlin
@Serializable
data class ProductDetailRoute(val productId: String, val fromSearch: Boolean = false)
```

Both approaches achieve the same goal. Solivagant uses Parcelable (Android-native serialization);
Navigation 3 uses Kotlin Serialization (more multiplatform-friendly). For projects already
using kmp-viewmodel's `@Parcelize`, solivagant's approach has no migration cost.

### Status

**Still relevant as a pattern. Superseded mechanistically by `@Serializable` routes in Navigation 3.**

---

## 2. NavRoot / NavRoute Semantic Split

### What it is

solivagant distinguishes two types of navigation destinations:

- `NavRoot` — owns a back stack. Navigating to a `NavRoot` saves the current back stack and
  activates (or creates) the root's own back stack.
- `NavRoute` — a regular entry pushed onto the current back stack.

This distinction is declared at the type level, not configured at runtime.

### Why it was interesting

The semantic split makes multi-backstack behaviour explicit and checked. You cannot accidentally
navigate to a `NavRoute` as if it were a root (it will not create a new stack) and you cannot
navigate to a `NavRoot` as a regular push (the `push(NavRoot)` method is distinct from
`push(NavRoute)`).

This models the common bottom-navigation pattern:

```
HomeRoot           → [ HomeRoot, ProductList, ProductDetail ]
SearchRoot         → [ SearchRoot, SearchResults ]
ProfileRoot        → [ ProfileRoot, Settings ]
```

Switching from Home to Search via bottom nav calls `navigator.navigateToRoot(SearchRoot)`. The
Home back stack is saved. Returning to Home restores it.

### Current relevance

Navigation 3 does not have an equivalent first-class concept of `NavRoot` vs `NavRoute` in its
current design (uncertain — needs verification). Bottom-navigation multi-backstack is handled
differently in different Navigation 3 patterns. solivagant's explicit split is simpler to reason
about at the cost of being less flexible.

**If Navigation 3 does not have an equivalent, this pattern deserves a standalone write-up.**

### Status

**Relevant. Unique to solivagant compared to Navigation 3's current approach.**

---

## 3. Navigation Commands via Channel (NavEventNavigator)

### What it is

`NavEventNavigator` is an injectable object that accepts navigation commands:

```kotlin
navigator.navigateTo(SearchRoute)
navigator.navigateBack()
navigator.navigateBackTo<HomeRoute>(inclusive = false)
navigator.replaceAll(OnboardingRoot)
```

These calls send a `NavEvent` into a `Channel.UNLIMITED`. The `NavHost` composable collects
events from this channel and applies them to the `MultiStack`.

`NavEventNavigator` is DI-injectable. A ViewModel holds a reference to it:

```kotlin
class HomeViewModel(
    private val navigator: NavEventNavigator,  // injected, not a Compose type
) : ViewModel() {
    fun onProductClicked(id: String) {
        navigator.navigateTo(ProductDetailRoute(id))
    }
}
```

### Why it was interesting

The navigation side effects are expressed as values (`NavEvent`), not as direct mutations of
a composable state. This has several benefits:

1. **Testability**: Unit tests can instantiate a `NavEventNavigator` and assert which events were
   emitted without running Compose.
2. **Decoupling**: ViewModels do not hold Compose or Activity references. They hold the navigator.
3. **Buffering**: Because it is a `Channel.UNLIMITED`, navigation commands are not dropped if
   the collector (NavHost) is temporarily not active.
4. **Composability**: Multiple navigation commands can be batched via `navigator.navigate { … }`.

### Current relevance

This pattern has no direct equivalent in Navigation 3 (which models back stack as a
`SnapshotStateList` mutated directly) or the official Navigation Compose (which provides a
`NavController` tied to the composition).

The Channel-based event pattern is a form of the Unidirectional Data Flow model where:
- State is in `MultiStack`.
- Events are `NavEvent` objects.
- The `NavHost` is the reducer that applies events to state.

This is a legitimate architectural choice with testability advantages that Navigation 3 does not
replicate by default.

### Status

**Still relevant as an architectural pattern. No official API equivalent. Worth documenting.**

---

## 4. Back Stack as an Explicit Data Structure (MultiStack)

### What it is

`MultiStack` is an internal class that explicitly models the multi-backstack state machine. It
holds an `ArrayList<Stack>` (one per `NavRoot`), a reference to the current `Stack`, and a
reference to the start `Stack`. State transitions are pure methods on this class:

```kotlin
multiStack.push(route: NavRoute)
multiStack.pop()
multiStack.popUpTo(destinationId, isInclusive)
multiStack.push(root: NavRoot, clearTargetStack: Boolean)
multiStack.resetToRoot(root: NavRoot)
multiStack.replaceAll(root: NavRoot)
```

The visible destinations are derived state: `computeVisibleEntries()` returns the topmost
screen entry plus any overlay entries above it.

`MultiStack` can be serialized to `MultiStackSavedState` (a `Parcelable` class) and restored via
`MultiStack.fromState(savedState, …)`.

### Why it was interesting

The back stack is a value that can be inspected, serialized, and reconstructed independently of
the Compose composition. The state machine logic is in plain Kotlin, not in a framework class.
This is the same insight that Navigation 3 later encoded: "the back stack should be observable
application state, not a hidden framework concept."

### Current relevance

Navigation 3 makes the back stack a `SnapshotStateList<Any>` that the application directly
manipulates. The concept of explicit, inspectable back stack state is now mainstream.

solivagant's back stack is less flexible (you go through the `MultiStack` API rather than
directly mutating the list), but more constrained and therefore harder to corrupt accidentally.

### Status

**Relevant. The design principle is now reflected in Navigation 3. solivagant's approach is
more constrained but safer.**

---

## 5. Overlay Destinations

### What it is

`OverlayDestination<T>` is a `NavDestination` variant for screens that are rendered on top of
the current screen without replacing it on the back stack. Dialogs and bottom sheets are the
typical use case.

```kotlin
@JvmField
val ConfirmDeleteDialog: NavDestination =
    OverlayDestination<ConfirmDeleteRoute> { route, modifier ->
        ConfirmDeleteDialog(modifier = modifier, route = route)
    }
```

Dismissing the overlay navigates back (`navigator.navigateBack()`), which pops the overlay entry
and re-shows the screen beneath it. The overlay and the screen beneath it are separate lifecycle
owners and ViewModel scopes.

### Why it was interesting

In standard Compose, dialogs are typically driven by a `Boolean` state flag inside the parent
screen composable. This couples the dialog's visibility to the parent's state management and makes
it impossible to navigate to a dialog directly from a different screen or from a ViewModel
without threading the flag through multiple layers.

Treating dialogs as navigation destinations makes deep-linking to dialogs possible and gives each
dialog its own ViewModel scope.

### Current relevance

Official Navigation Compose does support dialog destinations via `dialog { … }` builders. The
concept is standard, though the exact integration with multiplatform ViewModel scopes is not
guaranteed. solivagant's overlay mechanism is more general than just dialogs.

### Status

**Relevant. Well-designed. Covered by official Navigation Compose but worth noting as a
clean pattern for multiplatform apps.**

---

## 6. Result Delivery Between Destinations

### What it is

solivagant supports a type-safe result delivery mechanism between destinations:

```kotlin
// In the source destination's Navigator
val editResult = registerForNavigationResult<EditRoute, EditResult>()

// In the target destination
navigator.deliverNavigationResult(key, EditResult(updatedName))
```

The result is delivered as a `Parcelable` and collected via the `NavigationResultRequest`'s
`Flow`.

### Why it was interesting

The standard alternative is shared `StateFlow` or `ViewModel` communication, which requires
either shared ViewModels or event bus patterns. The result delivery mechanism is scoped to the
navigation pair that created it.

### Current relevance

Official Navigation 3 and Navigation Compose do not have a direct equivalent typed result
mechanism (Navigation Compose uses `SavedStateHandle` for result passing, which is less type-safe).

### Status

**Still unique. Worth documenting as a pattern even if deprecated from the library.**

---

## Summary Table

| Pattern | Still Relevant | Navigation 3 Equivalent | Unique to solivagant |
|---------|---------------|------------------------|----------------------|
| Typed route values | ✅ Yes | ✅ Yes (`@Serializable`) | No |
| NavRoot/NavRoute split | ✅ Yes | ⚠️ Partial / uncertain | Yes |
| Channel-based nav commands | ✅ Yes | ❌ No | Yes |
| Explicit MultiStack model | ✅ Yes | ✅ `SnapshotStateList` | Design differs |
| Overlay destinations | ✅ Yes | ✅ Yes (`dialog {}`) | No |
| Per-entry lifecycle scoping | ✅ Yes | ⚠️ Partial / uncertain | Partial |
| Typed result delivery | ✅ Yes | ❌ No | Yes |
