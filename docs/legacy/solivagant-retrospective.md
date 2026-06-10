# solivagant Retrospective

A technical audit of the solivagant navigation library, written mid-2025.

---

## Overview

**First release:** February 2024 (0.0.1)  
**Latest release:** 0.5.0 (August 2024)  
**Current development version:** 0.5.1-SNAPSHOT  
**Kotlin version at latest release:** 2.0.0  
**Compose Multiplatform version:** 1.6.11  
**License:** Apache 2.0  
**Gradle coordinates:** `io.github.hoc081098:solivagant-navigation`

solivagant is a Compose Multiplatform navigation library ported from
[Freeletics Khonshu Navigation](https://freeletics.github.io/khonshu/navigation/get-started/).
It provides type-safe routing, multi-backstack support, per-entry lifecycle and ViewModel scoping,
and platform-agnostic lifecycle events.

---

## Module Structure

| Module | Artifact ID | Purpose |
|--------|-------------|---------|
| `khonshu-navigation-core` | `solivagant-khonshu-navigation-core` | Route types, NavEventNavigator, NavigationSetup, event model |
| `navigation` | `solivagant-navigation` | NavHost composable, MultiStack, StackEntry lifecycle |
| `lifecycle` | `solivagant-lifecycle` | Multiplatform Lifecycle/LifecycleOwner/LifecycleRegistry |

The `lifecycle` module is a standalone dependency that can be used without navigation.

---

## Supported Targets

Android, JVM (Desktop), JS (IR), Wasm/JS, iOS (arm64, x64, simulatorArm64), macOS (arm64, x64),
watchOS (arm32, arm64, x64, simulatorArm64), tvOS (x64, simulatorArm64, arm64), Linux (x64),
Windows (mingwX64).

This is a **comprehensive target matrix** — more complete than most community KMP navigation
libraries at the time of writing.

---

## Public API Surface

### Core types (`khonshu-navigation-core`)

```
BaseRoute : Parcelable               // sealed root
  NavRoute : BaseRoute               // regular destination
  NavRoot  : BaseRoute               // owns a backstack (multi-stack)

NavDestination                       // sealed, describes a screen
  ContentDestination<T : BaseRoute>
    ScreenDestination<T>             // full-screen
    OverlayDestination<T>            // dialog / bottom sheet

NavEventNavigator                    // open class; Channel-backed nav command emitter
  implements Navigator, ResultNavigator, BackInterceptor

NavigationSetup(navigator)           // Composable; wires the navigator into the composition

NavHost(startRoute, destinations, …) // main Composable; renders the backstack

StackValidationMode                  // sealed: Strict / Lenient / Warning
NavHostTransitionAnimations          // data class; customisable per-event animations
SavedStateSupport                    // non-Android saved state / ViewModel store bridge
```

### Lifecycle module

```
Lifecycle                            // interface; mirrors AndroidX Lifecycle
LifecycleOwner                       // interface
LifecycleRegistry                    // strict state-machine implementation
LenientLifecycleRegistry             // flexible, moveTo(state) variant

LocalLifecycleOwner                  // CompositionLocal
repeatOnLifecycle(state, block)      // coroutine-safe lifecycle observer
flowWithLifecycle(state)             // Flow operator
collectAsStateWithLifecycle(…)       // Compose extension
LifecycleControllerEffect(…)         // JVM Desktop: tie lifecycle to WindowState
LifecycleOwnerComposeUIViewControllerDelegate  // iOS: tie lifecycle to UIViewController
```

---

## What the Library Does Well

### 1. Type-safe routes with zero string pollution

Routes are Kotlin data classes or objects implementing `NavRoute` / `NavRoot`. Arguments are
properties of the route, not query parameters. Navigation errors are compile-time errors:

```kotlin
@Parcelize
data class ProductDetailRoute(val productId: String) : NavRoute

// Usage — type-checked at compile time
navigator.navigateTo(ProductDetailRoute(productId = "abc-123"))
```

There is no route string, no argument bundle, no `navBackStackEntry.arguments?.getString(…)`.
The route instance is directly available as the typed `route` parameter of the destination
composable.

### 2. NavEventNavigator: navigation from outside Compose

The `NavEventNavigator` is an open class that emits `NavEvent` objects into an `Channel.UNLIMITED`
channel. ViewModels, use cases, or any non-Compose layer can call `navigator.navigateTo(…)` without
holding a composable reference. The `NavHost` collects events and applies them to the `MultiStack`.

This is a clean inversion: the ViewModel does not know about the UI stack; it only knows it wants
to transition to a new state.

### 3. NavRoot / NavRoute semantic separation

`NavRoot` destinations own a `Stack`. Multiple `Stack` objects are held in `MultiStack`. This
models the common bottom-navigation pattern explicitly in the type system:

- Switching to a `NavRoot` saves the current stack and restores (or creates) the target root's
  stack.
- Navigating within a `NavRoot` pushes `NavRoute` entries onto that root's stack.

There is no implicit rule about which destinations can be roots vs. leaf screens. The caller
declares intent at the type level.

### 4. Per-entry lifecycle and ViewModel scope

Each `StackEntry<T>` holds a `StackEntryLifecycleOwner` and (via `StackEntryViewModelStoreOwner`)
its own `ViewModelStore`. Lifecycle state advances from `CREATED → STARTED → RESUMED` as the entry
becomes visible and the transition completes, and reverses when the entry exits. This is the same
model as AndroidX Navigation Compose, but working on all platforms.

### 5. Overlay destinations

`OverlayDestination<T>` allows dialogs and bottom sheets to be navigation destinations. They are
rendered in the same `NavHost` composable as regular screens but are composited on top of the
current screen using `Modifier.matchParentSize()`. Their lifecycle is separate from the screen
beneath them. Dismissing an overlay destination is a navigation event, not a state flag inside a
composable.

### 6. StackValidationMode

`StackValidationMode.Strict` throws an exception when the stack enters an invalid state.
`StackValidationMode.Lenient` (default) silently ignores invalid transitions.
`StackValidationMode.Warning` logs a warning. This is a practical production safety valve: debug
builds can use `Warning.Debug` to surface mistakes without crashing users.

### 7. Animated transitions driven by StackEvent

Transition animations are chosen based on the type of navigation event (`Push`, `Pop`, `PushRoot`,
`ReplaceAll`, `Idle`). Enter/exit transitions are independently customisable per event type.
The default animations match AndroidX Navigation Compose defaults, so apps migrating feel familiar.

### 8. SavedStateSupport for non-Android platforms

`SavedStateSupport` implements `SaveableStateRegistry`, `SavedStateHandleFactory`, and
`ViewModelStoreOwner` in one object. On non-Android platforms, wrapping the root composable in
`savedStateSupport.ProvideCompositionLocals { … }` enables `rememberSaveable`, `SavedStateHandle`
in ViewModels, and `ViewModelStore` lifecycle management without any Android-specific code.

---

## What Is Weak or Risky

### 1. Still alpha

The README explicitly marks this library as alpha. The API surface has had breaking changes between
minor versions (e.g., `Modifier` parameter added to `ContentDestination.content` in 0.1.0). API
stability is not guaranteed.

### 2. Dependency on kmp-viewmodel

solivagant depends on kmp-viewmodel for `ViewModel`, `SavedStateHandle`, `Parcelable`, and
`@Parcelize`. As official multiplatform alternatives mature, this introduces a dual-dependency
burden. If kmp-viewmodel is deprecated, solivagant will need to either inline its dependencies or
migrate to official APIs.

### 3. Limited test coverage

The `commonTest` folder contains only 4 files: `StackEntryTest.kt`, `FakeCloseable.kt`,
`destinations.kt`, `routes.kt`. There are no integration tests for `NavHost` composable rendering,
`MultiStack` full state machine paths (though `StackEntry` is tested), or cross-platform state
restoration. The test coverage is sparse relative to the complexity of the back stack logic.

### 4. Internal complexity of MultiStack / Stack

`MultiStack.kt` and `Stack.kt` are the core of the library's correctness. They are complex
(multiple paths for `pop`, `push`, `resetToRoot`, `replaceAll`), the lifecycle logic is subtle,
and changes to these files carry high regression risk. Without comprehensive tests, this is a
maintenance liability.

### 5. NavEventNavigator is open

`NavEventNavigator` is an `open class`. Subclassing it to add domain-specific navigation methods
is an intended pattern. However, as the library evolves, changes to the base class (e.g., adding
new abstract-internal methods) could silently break subclasses. A `@Stable`-typed interface or
delegation pattern would be safer.

### 6. Khonshu attribution complexity

A large portion of `khonshu-navigation-core` and `navigation` is ported from Freeletics Khonshu.
The copyright headers are correct and Khonshu is Apache 2.0, so there is no licence issue. However,
any upstream changes to Khonshu's design are not automatically incorporated. The library is a fork
in practice, not a wrapper.

---

## Relationship to Modern Official APIs

### JetBrains Compose Multiplatform lifecycle (since ~1.6.0)

JetBrains ships `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` and related
artifacts. These provide `ViewModel`, `viewModelScope`, `SavedStateHandle`, and `LocalLifecycleOwner`
in multiplatform Compose. As of 2025 these artifacts are stable and recommended for new projects.

**Overlap:** solivagant's `lifecycle` module largely duplicates these. The `Lifecycle`,
`LifecycleOwner`, `LifecycleRegistry`, `collectAsStateWithLifecycle`, `repeatOnLifecycle`,
`flowWithLifecycle` APIs are now available from JetBrains. The solivagant lifecycle module has
value as a standalone, dependency-light implementation, but the duplication cost grows over time.

### Navigation 3 (`androidx.navigation3`)

Navigation 3 is a redesign of Navigation Compose announced in 2025. Its key design ideas include:
- `NavBackStack` as observable state (a `SnapshotStateList<Any>`) rather than a framework-managed
  implicit stack.
- Type-safe routes via Kotlin serialization.
- Separation of navigation state from UI rendering.
- First-class multiplatform intent (though full multiplatform availability is still in progress as
  of mid-2025).

**Comparison with solivagant:**
- solivagant routes are `Parcelable` data classes. Navigation 3 routes are `@Serializable` classes.
  Both achieve type safety; the mechanisms differ.
- solivagant's `MultiStack` is similar in spirit to Navigation 3's explicitly managed `NavBackStack`.
  Both treat the back stack as an observable data structure.
- Navigation 3 does not yet have a complete multiplatform story for iOS/Desktop as of mid-2025.
  solivagant works on all targets today.
- Navigation 3's back stack is directly manipulable by the app (it's a `SnapshotStateList`).
  solivagant's is manipulated via commands (`NavEvent`), which is a stronger separation of concerns
  but less flexible.
- Navigation 3 has no per-entry `LifecycleOwner` mechanism in its public API yet (uncertain —
  needs verification against latest Navigation 3 API).

**Assessment:** solivagant anticipated Navigation 3's conceptual direction (observable back stack
state, typed routes, explicit stack management). Navigation 3 is not yet a drop-in replacement for
solivagant on non-Android platforms.

---

## Does solivagant Still Compile on Modern Kotlin / Compose?

**Yes.** The library was updated to Kotlin 2.0.0 and Compose Multiplatform 1.6.11 in release 0.4.0
(July 2024) and 0.5.0 (August 2024). The snapshot branch targets Kotlin 2.0.10. No known
compilation failures exist at the time of this writing.

---

## Verdict

solivagant is a well-designed library that solved a real problem before official APIs existed. Its
core ideas — type-safe routes as data classes, explicit multi-backstack ownership, navigation
commands emitted from ViewModels, per-entry lifecycle scoping, and overlay destinations as first-
class navigation targets — remain sound.

However:
- The `lifecycle` module is increasingly duplicated by JetBrains official artifacts.
- Navigation 3, once stable and multiplatform, will cover most solivagant use cases.
- The library is in alpha and has sparse test coverage relative to its complexity.
- Maintenance requires keeping pace with Kotlin, Compose, and kmp-viewmodel releases.

See [maintenance/repository-status.md](../maintenance/repository-status.md) for the
keep/deprecate/archive recommendation.
