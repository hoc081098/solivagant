# Why These Libraries Existed

## The 2022–2023 KMP Ecosystem Gap

### Context

Both **solivagant** (first release: February 2024) and **kmp-viewmodel** (first release: February 2023)
were created during a specific window in the Kotlin Multiplatform timeline when:

1. **Compose Multiplatform was gaining practical adoption** (JetBrains 1.4–1.5 era) but had no
   first-party navigation library for non-Android targets.
2. **AndroidX Lifecycle** (ViewModel, SavedStateHandle, Lifecycle) was Android-only; no official
   multiplatform artifact existed.
3. **Navigation Compose** (`androidx.navigation:navigation-compose`) was Android-only. Using it in
   a shared Compose Multiplatform module was not possible.
4. Community libraries like **Decompose** and **Voyager** existed but made different architectural
   trade-offs (Decompose requires component trees; Voyager has a Screen-centric model that does not
   integrate with AndroidX ViewModel).
5. **Navigation 3** (the JetBrains / Google redesign of Navigation Compose) had not yet been
   announced, let alone stabilised.

The result was a real gap: developers writing Compose Multiplatform apps targeting Android, iOS,
and Desktop needed working navigation + ViewModel solutions that could share code across all targets.

---

## What Problem Each Library Solved

### kmp-viewmodel

**Problem:** The AndroidX `ViewModel` class, `ViewModelStore`, `ViewModelStoreOwner`,
`SavedStateHandle`, and the Kotlin Parcelize compiler plugin (`@Parcelize`) were all Android-only.
Any business logic written in common Kotlin that needed lifecycle-aware state caching, coroutine
scoping tied to screen lifetime, or state restoration after process death had to duplicate
implementations per target or avoid these patterns entirely.

**Solution:** kmp-viewmodel provided:
- A common `ViewModel` abstract class (delegating to AndroidX on Android, custom implementation on
  other targets) with a cancellable `viewModelScope` coroutine scope.
- `ViewModelStore`, `ViewModelStoreOwner`, `ViewModelFactory`, and `CreationExtras` as multiplatform
  types.
- `SavedStateHandle` accessible from common Kotlin code.
- `@Parcelize` / `Parcelable` usable in common source sets.
- Swift-friendly `Flow` wrappers (`NonNullFlowWrapper`, etc.) to ease Kotlin/Swift interop without
  third-party tools.
- Compose integration: `kmpViewModel`, `LocalViewModelStoreOwner`, `LocalSavedStateHandleFactory`.

### solivagant

**Problem:** Compose Multiplatform apps had no standard navigation solution. Existing options
required deep framework coupling (Decompose), lacked ViewModel/SavedStateHandle integration
(Voyager), or were Android-only (AndroidX Navigation Compose).

The specific requirements solivagant targeted:
- Type-safe routing without string-based URLs or reflection-heavy route registration.
- Navigation commands emitted from ViewModels or other non-Compose layers (testable without a
  Compose test environment).
- Multi-backstack support, most commonly needed for bottom navigation tabs.
- Navigation stack state saved and restored across Android configuration changes and process death.
- Lifecycle awareness at the individual navigation entry level (not just the Activity/ViewController
  level).
- Overlay destinations (dialogs, bottom sheets) rendered on top of the current screen without
  replacing the back stack entry.
- Compatibility with Koin, Koject, and manual DI patterns.

**Solution:** solivagant ported the architecture of [Freeletics Khonshu Navigation](https://freeletics.github.io/khonshu/navigation/get-started/)
to Kotlin Multiplatform and Compose Multiplatform, integrating it with kmp-viewmodel. It added:
- `NavRoute` / `NavRoot` / `BaseRoute` sealed interfaces (all `Parcelable`-serialisable).
- `NavDestination` / `ScreenDestination` / `OverlayDestination` as typed destination descriptors.
- `NavEventNavigator` — a Channel-backed object that decouples navigation intent from Compose UI.
- `NavHost` composable that renders the current backstack entry with animated transitions, correct
  `LifecycleOwner` and `ViewModelStoreOwner` per entry, and `SaveableStateHolder` support.
- `MultiStack` — an explicit, testable in-memory model of the multi-backstack state machine.
- A platform-agnostic `Lifecycle` / `LifecycleOwner` / `LifecycleRegistry` API for non-Android
  targets (inspired by Essenty and AndroidX Lifecycle).
- `SavedStateSupport` — a class that bridges Compose's `SaveableStateRegistry` with kmp-viewmodel's
  `SavedStateHandleFactory` and `ViewModelStore` for non-Android targets.

---

## What Made These Libraries Interesting Technically

### Channel-based navigation commands (solivagant)

Navigation state changes are triggered by sending events to a `Channel<NavEvent>` rather than
calling methods on a composable-owned state object. This decouples business logic from the
Compose composition tree. A ViewModel can call `navigator.navigateTo(MyRoute)` without having
access to any composable context, making navigation logic unit-testable with plain Kotlin tests.

### NavRoot / NavRoute semantic split (solivagant)

Instead of a flat list of destinations, solivagant distinguishes `NavRoot` (owns a backstack)
from `NavRoute` (entry on a backstack). This maps cleanly to real UI patterns where switching
bottom-nav tabs saves and restores independent back stacks. The model is explicit: there are no
implicit rules about which destinations can be roots.

### Parcelable in common code (kmp-viewmodel)

Making `@Parcelize` and `Parcelable` available in `commonMain` was a non-trivial engineering
challenge. On Android, it delegates to the real `android.os.Parcelable`. On other targets, it
provides a no-op marker interface with a custom Parcelize annotation so the compiler plugin still
works. This enabled routes, arguments, and saved state to be expressed as annotated data classes
in common code without `expect`/`actual` declarations for each class.

### Per-entry lifecycle and ViewModel scope (solivagant)

Each `StackEntry` has its own `LifecycleOwner` and `ViewModelStore`. When a destination is pushed,
its lifecycle advances to `STARTED` (visible but not fully resumed until transitions complete) and
then to `RESUMED`. When popped, it moves back to `CREATED` during the exit transition and is
destroyed when the transition ends. This mirrors how AndroidX Navigation Compose manages entry
lifecycles, but makes it work on all platforms.

### Swift interop wrappers (kmp-viewmodel)

`NonNullFlowWrapper<T>` and `NullableStateFlowWrapper<T>` are thin wrappers over Kotlin `Flow`
and `StateFlow` that expose a `subscribe(scope:onValue:)` method compatible with Swift's async
patterns without requiring manual use of `collect`. This was important at a time when Kotlin 1.9's
Swift/Objective-C export of generics was still incomplete.

---

## Timeline

| Date | Event |
|------|-------|
| Feb 2023 | kmp-viewmodel 0.0.1 released |
| Mar 2023 | kmp-viewmodel adds `SavedStateHandle` (0.2.0) |
| Sep 2023 | kmp-viewmodel adds Compose support (0.5.0) |
| Feb 2024 | solivagant 0.0.1 released |
| Mar 2024 | solivagant adds wasm, LenientLifecycleRegistry, SavedStateSupport (0.2.0) |
| Jul 2024 | solivagant updated to Kotlin 2.0, Compose MP 1.6.11 (0.4.0) |
| Aug 2024 | solivagant adds StackValidationMode (0.5.0) |
| 2024–2025 | JetBrains begins shipping official lifecycle-viewmodel multiplatform artifacts |
| 2025 | Navigation 3 (`androidx.navigation3`) enters alpha/beta |

---

## Conclusion

These libraries were not created out of an abundance of free time; they were created because the
official APIs did not exist yet. The design decisions made in both libraries are reasonable
responses to real constraints. Now that official APIs are maturing, the question is not whether
these libraries were well-designed (they were), but whether the ongoing maintenance cost is
justified for a problem that official tooling now covers.

See [maintenance/repository-status.md](../maintenance/repository-status.md) for the recommendation.
