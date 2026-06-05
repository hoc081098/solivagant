# Migrating from solivagant + kmp-viewmodel to Modern Official APIs

This guide maps solivagant and kmp-viewmodel concepts to official Compose Multiplatform and
AndroidX APIs available as of mid-2025.

> **Note:** This guide is forward-looking. Some official APIs mentioned (particularly Navigation 3
> on non-Android targets) are in alpha or beta and their stability timelines are uncertain.
> Verify API stability before migrating production code. Sections marked ⚠️ indicate areas
> where the official API is incomplete or not yet multiplatform.

---

## Part 1: kmp-viewmodel → Official JetBrains Lifecycle Artifacts

### Dependency change

**Before (kmp-viewmodel):**
```kotlin
// build.gradle.kts (commonMain)
implementation("io.github.hoc081098:kmp-viewmodel:0.8.0")
implementation("io.github.hoc081098:kmp-viewmodel-savedstate:0.8.0")
implementation("io.github.hoc081098:kmp-viewmodel-compose:0.8.0")
```

**After (official):**
```kotlin
// build.gradle.kts (commonMain)
implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.8.x")
implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.x")
implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.x")
```

> Check https://central.sonatype.com for the latest version of `org.jetbrains.androidx.lifecycle`.

### ViewModel base class

```kotlin
// kmp-viewmodel
import com.hoc081098.kmp.viewmodel.ViewModel

class SearchViewModel : ViewModel() { … }
```

```kotlin
// Official
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() { … }
```

The public API (`viewModelScope`, `onCleared`, `addCloseable`) is identical.

### viewModel composable function

```kotlin
// kmp-viewmodel-compose
import com.hoc081098.kmp.viewmodel.compose.kmpViewModel

val vm: SearchViewModel = kmpViewModel()
```

```kotlin
// Official
import androidx.lifecycle.compose.viewModel  // or androidx.lifecycle.viewmodel.compose.viewModel

val vm: SearchViewModel = viewModel()
```

### LocalViewModelStoreOwner and LocalSavedStateHandleFactory

```kotlin
// kmp-viewmodel-compose
import com.hoc081098.kmp.viewmodel.compose.LocalViewModelStoreOwner
import com.hoc081098.kmp.viewmodel.compose.LocalSavedStateHandleFactory
```

```kotlin
// Official
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
// SavedStateHandleFactory is accessed via CreationExtras in official APIs
```

### @Parcelize in commonMain

⚠️ **No direct official equivalent yet.** The kmp-viewmodel approach to `@Parcelize` in
`commonMain` remains the most ergonomic solution for projects that need Android Parcel
serialization in shared code.

**Option A (keep kmp-viewmodel savedstate):** Continue using
`io.github.hoc081098:kmp-viewmodel-savedstate` only for `@Parcelize` / `Parcelable` even
after migrating the ViewModel base class. This module is independent.

**Option B (use Kotlin Serialization for routes):** Replace `@Parcelize` routes with
`@Serializable` routes. This works well for Navigation 3 but requires that all route arguments
are serializable (not just parcelable). Kotlin Serialization is multiplatform and has no
platform-specific dependencies.

```kotlin
// kmp-viewmodel approach
@Parcelize
data class ProductDetailRoute(val id: String) : NavRoute

// Kotlin Serialization approach (for Navigation 3)
@Serializable
data class ProductDetailRoute(val id: String)
```

### Type-safe SavedStateHandle keys

⚠️ **No official equivalent.** The `NonNullSavedStateHandleKey` / `safe` API from
`kmp-viewmodel-savedstate` has no counterpart in official AndroidX or JetBrains APIs.

**Recommendation:** Keep using this API even after migrating to official ViewModel. The keys are
independent of the ViewModel base class and work with any `SavedStateHandle`. Extract the
key definitions file and add a note that it uses a thin utility from kmp-viewmodel.

Alternatively, copy the key classes into your project or create your own equivalent — the
implementation is small.

### Swift Flow wrappers

⚠️ **Partially superseded.** Kotlin 2.x improves Swift/Objective-C generic export. If you are
on Kotlin 2.0+ and using `@ObjCName` or Swift packages, you may no longer need the wrappers.
Test by consuming a `StateFlow<YourType>` from Swift without a wrapper and verify type safety.

If wrappers are still needed, keep using the `kmp-viewmodel` ones or copy them to your project.

---

## Part 2: solivagant → Navigation 3 ⚠️

> **Important caveat:** Navigation 3 (`androidx.navigation3`) is in alpha as of mid-2025. Its
> multiplatform support for iOS and Desktop is incomplete. **Do not migrate production apps to
> Navigation 3 until it reaches a stable release on your target platforms.**

The conceptual mapping is provided here for planning purposes.

### Dependency change

**Before (solivagant):**
```kotlin
implementation("io.github.hoc081098:solivagant-navigation:0.5.0")
```

**After (Navigation 3):**
```kotlin
// Check https://developer.android.com/jetpack/androidx/releases/navigation3 for latest
implementation("androidx.navigation3:navigation3-ui:1.0.0-alphaXX")
// + platform-specific entries once stable
```

### Route types

```kotlin
// solivagant
@Parcelize data object HomeRoute : NavRoot
@Parcelize data class DetailRoute(val id: String) : NavRoute
```

```kotlin
// Navigation 3
@Serializable data object HomeRoute
@Serializable data class DetailRoute(val id: String)
```

### NavHost / back stack host

```kotlin
// solivagant
NavHost(
    startRoute = HomeRoute,
    destinations = persistentSetOf(HomeDestination, DetailDestination),
    navEventNavigator = navigator,
)
```

```kotlin
// Navigation 3 (approximate — API still evolving)
val backStack = rememberNavBackStack(HomeRoute)
NavDisplay(
    backStack = backStack,
    entryDecorators = listOf(…),
) { entry ->
    when (val route = entry.key) {
        is HomeRoute -> HomeScreen(onNavigate = { backStack.add(DetailRoute(it)) })
        is DetailRoute -> DetailScreen(route.id)
        else -> error("Unknown route: $route")
    }
}
```

**Key differences:**
- Navigation 3 back stack (`SnapshotStateList<Any>`) is directly mutated by app code.
- solivagant back stack is mutated via `NavEventNavigator` commands.
- Navigation 3 has no built-in `NavEventNavigator` equivalent. Navigation calls happen inside
  composable lambdas passed to the entry rendering block.
- To replicate the "navigate from ViewModel" pattern, you need to either pass a callback lambda
  to the ViewModel or use a shared `StateFlow<NavigationIntent>` pattern (see below).

### Navigating from ViewModel in Navigation 3

Since Navigation 3 does not have `NavEventNavigator`, you need to implement the "ViewModel
triggers navigation" pattern yourself. One approach:

```kotlin
// ViewModel emits navigation intents
sealed interface NavIntent {
    data class GoToDetail(val id: String) : NavIntent
    data object GoBack : NavIntent
}

class HomeViewModel : ViewModel() {
    private val _navIntent = MutableSharedFlow<NavIntent>(extraBufferCapacity = 1)
    val navIntent: SharedFlow<NavIntent> = _navIntent

    fun onProductClicked(id: String) {
        _navIntent.tryEmit(NavIntent.GoToDetail(id))
    }
}

// Screen composable wires the intent to the back stack
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    backStack: NavBackStack,
) {
    LaunchedEffect(Unit) {
        viewModel.navIntent.collect { intent ->
            when (intent) {
                is NavIntent.GoToDetail -> backStack.add(DetailRoute(intent.id))
                NavIntent.GoBack -> backStack.removeLastOrNull()
            }
        }
    }
    // … UI
}
```

This is more boilerplate than `NavEventNavigator` but achieves the same separation of concerns.

### Multi-backstack

⚠️ **Navigation 3 multi-backstack support is not yet documented as a standard feature.**
Implement multiple `NavBackStack` instances (one per tab) manually and switch between them.

solivagant's `NavRoot` / multi-backstack model is significantly more ergonomic for this use case
until Navigation 3 adds explicit support.

### Overlay destinations (dialogs)

```kotlin
// solivagant
@Parcelize data object ConfirmDeleteRoute : NavRoute

val ConfirmDeleteDestination: NavDestination =
    OverlayDestination<ConfirmDeleteRoute> { _, modifier ->
        ConfirmDeleteDialog(modifier = modifier, onConfirm = { navigator.navigateBack() })
    }
```

Navigation 3 does not have an `OverlayDestination` concept. Use standard Compose dialog/modal
patterns or Navigation 3's `dialog` support if available.

---

## Part 3: Pieces That Can Be Kept Regardless

These kmp-viewmodel pieces can be used alongside official JetBrains lifecycle artifacts and
alongside any navigation library:

| Module | Can be used with official ViewModel? | Notes |
|--------|--------------------------------------|-------|
| `kmp-viewmodel-savedstate` (type-safe keys) | ✅ Yes | Keys work with any `SavedStateHandle` |
| `kmp-viewmodel-savedstate` (`@Parcelize`) | ✅ Yes | Independent of ViewModel base class |
| `kmp-viewmodel` Swift wrappers | ✅ Yes | Independent of lifecycle framework |
| `kmp-viewmodel-koin-compose` | ⚠️ Check version compat | Koin may need updating |
| solivagant lifecycle module | ✅ Yes | Standalone, no navigation dependency |

---

## Summary

| Migration | Effort | Risk | When to start |
|-----------|--------|------|---------------|
| kmp-viewmodel ViewModel → JetBrains lifecycle | Low | Low | Now (stable) |
| kmp-viewmodel savedstate keys | ❌ Keep or copy | None | Not needed |
| solivagant → Navigation 3 | High | High | Wait for Navigation 3 stability on all targets |
| solivagant lifecycle → JetBrains lifecycle | Medium | Low-Medium | Now if needed |
