# Change Log

## [Unreleased] - TBD

### Update dependencies
- [KotlinX Coroutines `1.8.0`](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.8.0).
- [KMP ViewModel `0.7.0`](https://github.com/hoc081098/kmp-viewmodel/releases/tag/0.7.0).

### Changed

- **Breaking**: Add a `Modifier`parameter to `content` of `NavDestination`:
     ```kotlin
     public val content: @Composable (route: T, modifier: Modifier) -> Unit
     ```

- **New**: Add optional `transitionAnimations` parameter to `NavHost` Composable functions. Animations can be overriden with `NavHostDefaults.transitionAnimations` or disabled with `NavHostTransitionAnimations.noAnimations`. Default animations are the same as default animations in AndroidX's `NavHost`.

## [0.0.1] - Feb 7, 2024

- Initial release of `solivagant` 🔆.
  Compose Multiplatform Navigation - 🌸 Pragmatic, type safety navigation for Compose Multiplatform.
  Based on [Freeletics Khonshu Navigation](https://freeletics.github.io/khonshu/navigation/get-started/).
  ♥️ ViewModel, SavedStateHandle, Lifecycle, Multi-Backstacks, and more...

- Dependencies
  - [Kotlin `1.9.22`](https://github.com/JetBrains/kotlin/releases/tag/v1.9.22).
  - [JetBrains Compose Multiplatform `1.5.12`](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.5.12).
  - [KMP ViewModel `0.6.2`](https://github.com/hoc081098/kmp-viewmodel/releases/tag/0.6.2).
  - [KotlinX Coroutines `1.7.3`](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.7.3).
  - [KotlinX Collections Immutable `0.3.7`](https://github.com/Kotlin/kotlinx.collections.immutable/releases/tag/v0.3.7).

[Unreleased]: https://github.com/hoc081098/solivagant/compare/0.0.1...HEAD

[0.0.1]: https://github.com/hoc081098/solivagant/releases/tag/0.0.1
