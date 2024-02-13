# solivagant 🔆

## 🔆 Compose Multiplatform Navigation - 🌸 Pragmatic, type safety navigation for Compose Multiplatform. Based on [Freeletics Khonshu Navigation](https://freeletics.github.io/khonshu/navigation/get-started/). ♥️ ViewModel, SavedStateHandle, Lifecycle, Multi-Backstacks, Transitions, Back-press handling, and more...

[![maven-central](https://img.shields.io/maven-central/v/io.github.hoc081098/solivagant-navigation)](https://search.maven.org/search?q=g:io.github.hoc081098%20solivagant-navigation)
[![codecov](https://codecov.io/gh/hoc081098/solivagant/branch/master/graph/badge.svg?token=jBFg12osvP)](https://codecov.io/gh/hoc081098/solivagant)
[![Build and publish snapshot](https://github.com/hoc081098/solivagant/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/solivagant/actions/workflows/build.yml)
[![Build sample](https://github.com/hoc081098/solivagant/actions/workflows/sample.yml/badge.svg)](https://github.com/hoc081098/solivagant/actions/workflows/sample.yml)
[![Publish Release](https://github.com/hoc081098/solivagant/actions/workflows/publish-release.yml/badge.svg)](https://github.com/hoc081098/solivagant/actions/workflows/publish-release.yml)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin version](https://img.shields.io/badge/Kotlin-1.9.22-blueviolet?logo=kotlin&logoColor=white)](https://github.com/JetBrains/kotlin/releases/tag/v1.9.22)
[![KotlinX Coroutines version](https://img.shields.io/badge/Kotlinx_Coroutines-1.7.3-blueviolet?logo=kotlin&logoColor=white)](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.7.3)
[![Compose Multiplatform version](https://img.shields.io/badge/Compose_Multiplatform-1.5.12-blueviolet?logo=kotlin&logoColor=white)](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.5.12)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fhoc081098%2Fsolivagant&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

![badge][badge-android]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-js-ir]
![badge][badge-nodejs]
![badge][badge-linux]
![badge][badge-windows]
![badge][badge-ios]
![badge][badge-mac]
![badge][badge-watchos]
![badge][badge-tvos]
![badge][badge-apple-silicon]

- Integrates with `Jetbrains Compose Multiplatform` seamlessly and easily.

- Integrates with [**kmp-viewmodel**](https://github.com/hoc081098/kmp-viewmodel) library seamlessly and smoothly
  - Stack entry scoped `ViewModel`, exists as long as the
    stack entry is on the navigation stack, including the configuration changes on `Android`.

  - Supports `SavedStateHandle`, used to save and restore data over configuration changes
    or process death on `Android`.

- The **navigation stack state** is saved and restored automatically over configuration changes and process
  death on `Android`.
  On other platforms, you can use a support class provided by this library to store the navigation stack state
  as long as you want.

- **Type safety navigation**, easy to pass data between destinations.
  No more `String` route and dynamic query parameters.
  The `Solivagant` library uses `NavRoute`s and `NavRoot`s to define routes that can be navigated to.
  Arguments can be defined as part of the route (a.ka. properties of the route class) and are type safe.
  Each `NavRoute` and `NavRoot` has a corresponding `NavDestination` that describes the UI (a.k.a `@Composable`) of the
  route.

- Supports **Multi-Backstacks**, this is most commonly used in apps that use bottom navigation to
  separate the back stack of each tab.
  See [Freeletics Khonshu Navigation - Multiple back stacks](https://freeletics.github.io/khonshu/navigation/back-stacks/)
  for more details.

- Supports `LifecycleOwner`, `Lifecycle` events and states, similar to `AndroidX Lifecycle` library.

<p align="center">
    <img src="https://github.com/hoc081098/solivagant/raw/master/logo.png" width="400">
</p>

> [!NOTE]
> This library is still in alpha, so the API may change in the future.

## Credits

- Most of the code in `solivagant-khonshu-navigation-core` and `solivagant-navigation` libraries is
  taken from [Freeletics Khonshu Navigation](https://freeletics.github.io/khonshu/navigation/get-started/),
  and ported to `Kotlin Multiplatform` and `Compose Multiplatform`.

- The `solivagant-lifecycle` library is inspired
  by [Essenty Lifecycle](https://github.com/arkivanov/Essenty?tab=readme-ov-file#lifecycle),
  and [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle).

## Author: [Petrus Nguyễn Thái Học](https://github.com/hoc081098)

Liked some of my work? Buy me a coffee (or more likely a beer)

<a href="https://www.buymeacoffee.com/hoc081098" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" height=64></a>

## Docs

### **0.x release** docs: https://hoc081098.github.io/solivagant/docs/0.x

### Snapshot docs: https://hoc081098.github.io/solivagant/docs/latest

## Installation

```kotlin
allprojects {
  repositories {
    [...]
    mavenCentral()
  }
}
```

```kotlin
implementation("io.github.hoc081098:solivagant-navigation:0.0.1")
```

## Getting started

The library is ported from `Freeletics Khonshu Navigation` library, so the concepts is similar.
You can read the [Freeletics Khonshu Navigation](https://freeletics.github.io/khonshu/navigation/get-started/) to
understand
the concepts.

### 1. Create `NavRoot`s, `NavRoute`s

```kotlin
@Immutable
@Parcelize
data object StartScreenRoute : NavRoot

@Immutable
@Parcelize
data object SearchProductScreenRoute : NavRoute
```

> [!NOTE]
> `@Parcelize` is provided by `kmp-viewmodel-savedstate` library.
> See [kmp-viewmodel-savedstate](https://github.com/hoc081098/kmp-viewmodel?tab=readme-ov-file#2-kmp-viewmodel-savedstate-library) for more details.

### 2. Create `NavDestination`s along with `Composable`s and `ViewModel`s

- `StartScreen`: `StartScreenDestination`, `@Composable fun StartScreen()` and `StartViewModel`

```kotlin

@JvmField
val StartScreenDestination: NavDestination =
  ScreenDestination<StartScreenRoute> { StartScreen() }

@Composable
internal fun StartScreen(
  modifier: Modifier = Modifier,
  viewModel: StartViewModel = koinKmpViewModel(),
) {
  // UI Composable
}

internal class StartViewModel(
  // used to trigger navigation actions from outside the view layer (e.g. from a ViewModel).
  // Usually, it is singleton object, or the host Activity retained scope.
  private val navigator: NavEventNavigator,
) : ViewModel() {
  internal fun navigateToProductsScreen() = navigator.navigateTo(ProductsScreenRoute)
  internal fun navigateToSearchProductScreen() = navigator.navigateTo(SearchProductScreenRoute)
}
```

- `SearchProductScreen`: `SearchProductScreenDestination`, `@Composable fun SearchProductsScreen()`
  and `SearchProductsViewModel`

```kotlin
@JvmField
val SearchProductScreenDestination: NavDestination =
  ScreenDestination<SearchProductScreenRoute> { SearchProductsScreen() }

@Composable
internal fun SearchProductsScreen(
  modifier: Modifier = Modifier,
  viewModel: SearchProductsViewModel = koinKmpViewModel<SearchProductsViewModel>(),
) {
  // UI Composable
}

internal class SearchProductsViewModel(
  private val searchProducts: SearchProducts,
  private val savedStateHandle: SavedStateHandle,
  // used to trigger navigation actions from outside the view layer (e.g. from a ViewModel).
  // Usually, it is singleton object, or the host Activity retained scope.
  private val navigator: NavEventNavigator,
) : ViewModel() {
  fun navigateToProductDetail(id: Int) {
    navigator.navigateTo(ProductDetailScreenRoute(id))
  }
}
```

### 3. Setup

- Gather all `NavDestination`s in a set and use `NavEventNavigator` to trigger navigation actions.

```kotlin
@Stable
private val AllDestinations: ImmutableSet<NavDestination> = persistentSetOf(
  StartScreenDestination,
  SearchProductScreenDestination,
  // and more ...
)

@Composable
fun MyAwesomeApp(
  // used to trigger navigation actions from outside the view layer (e.g. from a ViewModel).
  // Usually, it is singleton object, or the host Activity retained scope.
  navigator: NavEventNavigator,
  modifier: Modifier = Modifier,
) {
  // BaseRoute is the parent interface of NavRoute and NavRoot.
  // It implements Parcelable, so it can be used with rememberSavable.
  var currentRoute: BaseRoute? by rememberSavable { mutableStateOf(null) }

  NavHost(
    modifier = modifier,
    // route to the screen that should be shown initially
    startRoute = StartScreenRoute,
    // should contain all destinations that can be navigated to
    destinations = AllDestinations,
    navEventNavigator = navigator,
    destinationChangedCallback = { currentRoute = it },
  )
}
```

- To display `MyAwesomeApp` on `Android`, use `setContent` in `Activity` or `Fragment`.

```kotlin
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle) {
    super.onCreate()

    // navigator can be retrieved from the DI container, such as Koin, Dagger Hilt, etc.

    setContent {
      MyAwesomeApp(
        navigator = navigator
      )
    }
  }
}
```

- To display `MyAwesomeApp` on `Desktop`, ... TBD ... please check out [samples/sample/desktop/src/commonMain/kotlin/com/hoc081098/solivagant/sample/main.kt](https://github.com/hoc081098/solivagant/blob/e47468b13fbd98c619cd973cd470036090ceed43/samples/sample/desktop/src/commonMain/kotlin/com/hoc081098/solivagant/sample/main.kt#L22-L68)

- To display `MyAwesomeApp` on `iOS/tvOS/watchOS`, ... TBD ... please check out [samples/sample/iosApp/iosApp/ContentView.swift](https://github.com/hoc081098/solivagant/blob/e47468b13fbd98c619cd973cd470036090ceed43/samples/sample/iosApp/iosApp/ContentView.swift#L19-L60)

### 4.Use `NavEventNavigator` in `ViewModel` s or in `@Composable` s to trigger navigation actions

```kotlin
// navigate to the destination that the given route leads to
navigator.navigateTo(DetailScreenRoute("some-id"))
// navigate up in the hierarchy
navigator.navigateUp()
// navigate to the previous destination in the backstack
navigator.navigateBack()
// navigate back to the destination belonging to the referenced route and remove all destinations
// in between from the back stack, depending on inclusive the destination
navigator.navigateBackTo<MainScreenRoute>(inclusive = false)
```

## Samples

- [Complete sample](https://github.com/hoc081098/solivagant/tree/master/samples/sample): a complete sample
  that demonstrates how to use `solivagant-navigation` in `Compose Multiplatform (Android, Desktop, iOS)`
  - `solivagant-navigation` for navigation in Compose Multiplatform.
  - `kmp-viewmodel` to share `ViewModel` and `SavedStateHandle`.
  - `Koin DI`.

- [Simple sample](https://github.com/hoc081098/solivagant/tree/master/samples/simple): a simple sample
  that demonstrates how to use `solivagant-navigation` in `Compose Multiplatform (Android, Desktop, iOS)` to
  switch between tabs (bottom navigation), but can keep the back stack state of each tab.
  Basically, it's a **multi-backstack** demo sample.

- [Compose Multiplatform KmpViewModel KMM Unsplash Sample](https://github.com/hoc081098/Compose-Multiplatform-KmpViewModel-KMM-Unsplash-Sample): A KMP template of the Unsplash App using Compose multiplatform for Android, Desktop, iOS. Share everything including data, domain, presentation, and UI.

## Roadmap

- [ ] Add more tests
- [ ] Add more samples
- [ ] Add docs
- [ ] Review supported targets
- [ ] Polish and improve the implementation and the public API
- [ ] Support transition when navigating
- [ ] Support more targets such as wasm, watchOS, tvOS, etc...

## License

```license
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/
```

[badge-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat

[badge-android-native]: http://img.shields.io/badge/support-[AndroidNative]-6EDB8D.svg?style=flat

[badge-wearos]: http://img.shields.io/badge/-wearos-8ECDA0.svg?style=flat

[badge-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat

[badge-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat

[badge-js-ir]: https://img.shields.io/badge/support-[IR]-AAC4E0.svg?style=flat

[badge-nodejs]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat

[badge-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat

[badge-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat

[badge-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat

[badge-apple-silicon]: http://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg?style=flat

[badge-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat

[badge-mac]: http://img.shields.io/badge/-macos-111111.svg?style=flat

[badge-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat

[badge-tvos]: http://img.shields.io/badge/-tvos-808080.svg?style=flat
