# Change Log

## [Unreleased] - TBD

- TDB

## [0.4.0] - Jul 22, 2024

### Update dependencies

- [Kotlin `2.0.0` 🎉](https://github.com/JetBrains/kotlin/releases/tag/v2.0.0).
- [JetBrains Compose Multiplatform `1.6.11` 🎉](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.6.11).
- [KMP ViewModel `0.8.0` 🎉](https://github.com/hoc081098/kmp-viewmodel/releases/tag/0.8.0).
- [KotlinX Coroutines `1.9.0-RC`](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.9.0-RC).
- [AndroidX Lifecycle `2.8.3`](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.3).
- [AndroidX Compose Activity `1.9.0`](https://developer.android.com/jetpack/androidx/releases/activity#1.9.0).
- [AndroidX Annotation `1.8.0`](https://developer.android.com/jetpack/androidx/releases/annotation#1.8.0).
- [Benasher44 UUID `0.8.4`](https://github.com/benasher44/uuid/releases/tag/0.8.4).

## [0.3.0] - Mar 13, 2024

- Fix an issue where a wrong host `LifecycleOwner` can be used (#62).
- Rework internal implementation and refactor code (#62).
- Handle `startRoute` changes properly (#62).

## [0.2.2] - Mar 9, 2024

### Fixed

- `NavHost`: handle the host `LifecycleOwner` properly to avoid memory leaks (#61).

## [0.2.1] - Mar 8, 2024

### Fixed

- Fix an issue where `SaveableStateHolder` would not be properly cleared when the destination
  is removed from the back stack after configuration changes on Android (#59).

## [0.2.0] - Mar 7, 2024

### Update dependencies

- [JetBrains Compose Multiplatform `1.6.0` 🎉](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.6.0).
- [KMP ViewModel `0.7.1`](https://github.com/hoc081098/kmp-viewmodel/releases/tag/0.7.1).

### Added

- **New**: Add support for Kotlin/Wasm (`wasmJs` target) 🎉.
- **New**: Add `LenientLifecycleRegistry`, a `LifecycleRegistry` without the state checking,
  and has `moveTo` method to move to a specific state. This is a flexible version
  of `LifecycleRegistry`.
- **New**: Add `rememberWindowLifecycleOwner()` for Desktop (JVM) platform.
- **New**: Add `SavedStateSupport.ProvideCompositionLocals` and `SavedStateSupport.ClearOnDispose`
  for non-Android platforms.

### Fixed

- **Fixed**: an issue where `LocalLifecycleOwner` provided the wrong `LifecycleOwner` to the content
  of `NavHost` composable.

### Sample

- **New**:
  Add [Solivagant Wasm Sample](https://github.com/hoc081098/solivagant/tree/master/samples/solivagant-wasm-sample).
  You can open the web application by running
  the `./gradlew :samples:solivagant-wasm-sample:wasmJsBrowserDevelopmentRun` Gradle task.

## [0.1.1] - Feb 25, 2024

### Fixed

- Add workaround for issue
  [JetBrains/compose-multiplatform #3147 - Kotlin/Native can't use T::class in inline function of @Composable for iOS](https://github.com/JetBrains/compose-multiplatform/issues/3147).

### Example, docs and tests

-
Add [Compose Multiplatform Todo solivagant Sample](https://github.com/hoc081098/Compose-Multiplatform-Todo-solivagant-Sample):
A KMP template of the Todo App using Compose multiplatform for Android, Desktop, iOS and Web.
Share everything including data, domain, presentation, and UI.

## [0.1.0] - Feb 19, 2024

### Update dependencies

- [KotlinX Coroutines `1.8.0`](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.8.0).
- [KMP ViewModel `0.7.0`](https://github.com/hoc081098/kmp-viewmodel/releases/tag/0.7.0).

### Added

- **New**: Add optional `transitionAnimations` parameter to `NavHost` @Composable functions.
  Animations can be overridden with `NavHostDefaults.transitionAnimations`
  or disabled with `NavHostTransitionAnimations.noAnimations`.
  Default animations are the same as default animations in AndroidX's `NavHost`.

### Changed

- **Breaking**: Add a `Modifier` parameter to `content` of `NavDestination`:
  ```kotlin
  @InternalNavigationApi
  public sealed interface ContentDestination<T : BaseRoute> : NavDestination {
    // other members...
    public val content: @Composable (route: T, modifier: Modifier) -> Unit
  }
  ```

  This change effects `ScreenDestination` and `OverlayDestination` as well.
  The `modifier` parameter should be passed to the `content` of `NavDestination`
  (a.k.a the root `@Composable` of the destination), for example:
  ```kotlin
  @Immutable
  @Parcelize
  data class DetailScreenRoute(val id: String) : NavRoute

  @JvmField
  val DetailScreenDestination = ScreenDestination<DetailScreenRoute> { route, modifier ->
    DetailScreen(modifier = modifier, route = route)
  }

  @Composable
  internal fun DetailScreen(modifier: Modifier, route: DetailScreenRoute) {
    Scaffold(
      modifier = modifier, // <--- Pass the modifier to the root @Composable
      topBar = { /* ... */ },
    ) {
      //* ... */
    }
  }
  ```

## [0.0.1] - Feb 7, 2024

- Initial release of `solivagant` 🔆.
  Compose Multiplatform Navigation - 🌸 Pragmatic, type safety navigation for Compose Multiplatform.
  Based
  on [Freeletics Khonshu Navigation](https://freeletics.github.io/khonshu/navigation/get-started/).
  ♥️ ViewModel, SavedStateHandle, Lifecycle, Multi-Backstacks, and more...

- Dependencies
  - [Kotlin `1.9.22`](https://github.com/JetBrains/kotlin/releases/tag/v1.9.22).
  - [JetBrains Compose Multiplatform `1.5.12`](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.5.12).
  - [KMP ViewModel `0.6.2`](https://github.com/hoc081098/kmp-viewmodel/releases/tag/0.6.2).
  - [KotlinX Coroutines `1.7.3`](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.7.3).
  - [KotlinX Collections Immutable `0.3.7`](https://github.com/Kotlin/kotlinx.collections.immutable/releases/tag/v0.3.7).

[Unreleased]: https://github.com/hoc081098/solivagant/compare/0.4.0...HEAD

[0.4.0]: https://github.com/hoc081098/solivagant/releases/tag/0.4.0

[0.3.0]: https://github.com/hoc081098/solivagant/releases/tag/0.3.0

[0.2.2]: https://github.com/hoc081098/solivagant/releases/tag/0.2.2

[0.2.1]: https://github.com/hoc081098/solivagant/releases/tag/0.2.1

[0.2.0]: https://github.com/hoc081098/solivagant/releases/tag/0.2.0

[0.1.1]: https://github.com/hoc081098/solivagant/releases/tag/0.1.1

[0.1.0]: https://github.com/hoc081098/solivagant/releases/tag/0.1.0

[0.0.1]: https://github.com/hoc081098/solivagant/releases/tag/0.0.1
