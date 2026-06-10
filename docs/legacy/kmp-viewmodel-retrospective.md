# kmp-viewmodel Retrospective

A technical audit of the kmp-viewmodel library, written mid-2025.

---

## Overview

**First release:** February 2023 (0.0.1)  
**Latest release:** 0.8.0 (June 2024)  
**Kotlin version at latest release:** 2.0.0  
**License:** MIT  
**Gradle coordinates (core):** `io.github.hoc081098:kmp-viewmodel`

kmp-viewmodel is a Kotlin Multiplatform library that brings the AndroidX `ViewModel` programming
model — lifecycle-aware state holders, `viewModelScope`, `SavedStateHandle`, `@Parcelize` — to
common Kotlin code shared across Android, iOS, Desktop, and Web targets.

---

## Module Structure

| Module | Artifact ID | Purpose |
|--------|-------------|---------|
| `viewmodel` | `kmp-viewmodel` | `ViewModel`, `ViewModelStore`, `ViewModelFactory`, `CreationExtras`, Swift `Flow` wrappers |
| `viewmodel-savedstate` | `kmp-viewmodel-savedstate` | `SavedStateHandle`, `Parcelable`, `@Parcelize`, type-safe key API |
| `viewmodel-compose` | `kmp-viewmodel-compose` | Compose integration: `kmpViewModel`, `LocalViewModelStoreOwner`, `LocalSavedStateHandleFactory` |
| `viewmodel-koin` | `kmp-viewmodel-koin` | Koin DI integration |
| `viewmodel-koin-compose` | `kmp-viewmodel-koin-compose` | `koinKmpViewModel` composable function |
| `viewmodel-koject` | `kmp-viewmodel-koject` | Koject DI integration |
| `viewmodel-koject-compose` | `kmp-viewmodel-koject-compose` | `kojectKmpViewModel` composable function |

---

## Supported Targets

Android, JVM (Desktop), JS (IR), Wasm/JS, iOS (arm64, x64, simulatorArm64), watchOS (arm32, arm64,
x64, simulatorArm64), tvOS (x64, simulatorArm64, arm64), macOS (x64, arm64).

---

## Public API Surface

### `viewmodel` module

```kotlin
// Core ViewModel
expect abstract class ViewModel {
    constructor()
    constructor(vararg closeables: Closeable)
    val viewModelScope: CoroutineScope
    protected open fun onCleared()
    fun addCloseable(closeable: Closeable)
}

// Store management
class ViewModelStore
interface ViewModelStoreOwner
interface ViewModelFactory<VM : ViewModel>
interface Closeable

// Creation
class CreationExtras
class MutableCreationExtrasBuilder
fun buildCreationExtras(builder: MutableCreationExtrasBuilder.() -> Unit): CreationExtras
val VIEW_MODEL_KEY: CreationExtrasKey<String>
```

### `viewmodel-savedstate` module

```kotlin
// Platform-agnostic Parcelable
interface Parcelable                        // no-op on non-Android
annotation class Parcelize                  // delegates to kotlin-parcelize on Android

// SavedStateHandle
class SavedStateHandle
interface SavedStateHandleFactory
val SAVED_STATE_HANDLE_FACTORY_KEY: CreationExtrasKey<SavedStateHandleFactory>
fun CreationExtras.createSavedStateHandle(): SavedStateHandle

// Type-safe key API
class NonNullSavedStateHandleKey<T>
class NullableSavedStateHandleKey<T>
// Factory methods: .string(), .int(), .boolean(), .serializable(), .parcelable(), ...

// Safe accessor
val SavedStateHandle.safe: SafeSavedStateHandle
infix fun <T> SavedStateHandle.safe(block: (SafeSavedStateHandle) -> T): T
```

### `viewmodel-compose` module

```kotlin
@Composable fun <VM : ViewModel> kmpViewModel(
    factory: ViewModelFactory<VM>,
    viewModelStoreOwner: ViewModelStoreOwner = …,
    key: String? = null,
    extras: CreationExtras = …,
): VM

// Composition Locals
val LocalViewModelStoreOwner: ProvidableCompositionLocal<ViewModelStoreOwner>
val LocalSavedStateHandleFactory: ProvidableCompositionLocal<SavedStateHandleFactory>
```

### Swift wrappers (`viewmodel` module)

```kotlin
class NonNullFlowWrapper<T : Any>(flow: Flow<T>)
class NullableFlowWrapper<T>(flow: Flow<T>)
class NonNullStateFlowWrapper<T : Any>(flow: StateFlow<T>)
class NullableStateFlowWrapper<T>(flow: StateFlow<T>)

// Each has: .value, .subscribe(scope, onValue), .collect(scope, onValue)
```

---

## What the Library Does Well

### 1. Transparent multiplatform ViewModel

On Android, `ViewModel` is a typealias or subclass of `androidx.lifecycle.ViewModel`. On other
targets, it is a pure Kotlin implementation. This means code written against `kmp-viewmodel.ViewModel`
works without modification on Android (with full AndroidX lifecycle integration) and on other
platforms (with a clean Kotlin implementation).

The `viewModelScope` mirrors `androidx.lifecycle.viewModelScope` on Android (Main.immediate,
cancelled in `onCleared`). On non-Android targets, it also uses Main.immediate or Main, ensuring
consistent threading behaviour across platforms.

### 2. @Parcelize in common code

This is perhaps the library's most practical contribution. Making `@Parcelize` work in
`commonMain` requires a compiler plugin that behaves differently per target:
- On Android, the annotation is passed through to the real kotlin-parcelize plugin.
- On non-Android targets, the annotation is a no-op marker.

The `Parcelable` interface similarly has a no-op implementation on non-Android targets. The result
is that data classes representing navigation routes, arguments, or saved state can be written once
in `commonMain` with full Android Parcel serialization support and no conditional compilation.

This pattern was non-trivial to implement (and in Kotlin 2.0.0 required an explicit
`freeCompilerArgs` addition for the parcelize plugin) and saved significant boilerplate.

### 3. Type-safe SavedStateHandle keys

The `NonNullSavedStateHandleKey<T>` / `NullableSavedStateHandleKey<T>` API addresses a real
footgun in AndroidX `SavedStateHandle`: keys are `String`-typed and the type of the value is
implicit. The type-safe key API associates a `String` key, a default value, and a type in a
single object, then provides `safe` extension functions that enforce the type:

```kotlin
private val queryKey = NonNullSavedStateHandleKey.string("query", defaultValue = "")

// Compile-time type: String (not Any?)
val query: String = savedStateHandle.safe[queryKey]
```

This is a genuinely useful pattern regardless of whether the underlying `SavedStateHandle` is from
kmp-viewmodel or official AndroidX.

### 4. Swift-friendly Flow wrappers

Before Kotlin's Swift export of generics was production-ready, consuming `StateFlow<T>` in Swift
required casts and manual threading management. The `NonNullStateFlowWrapper<T>` class provides
a Swift-callable `subscribe(scope:onValue:)` method and a typed `value` property. This is
especially important for teams sharing ViewModels between Compose Multiplatform UI and SwiftUI.

### 5. DI integration modules

The `koin-compose` and `koject-compose` modules provide `koinKmpViewModel` and
`kojectKmpViewModel` composable functions that integrate the `ViewModelFactory` pattern with
popular KMP DI containers. These are glue layers that remove boilerplate for common project setups.

---

## What Is Weak or Risky

### 1. Official multiplatform ViewModel is now available

JetBrains ships `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` as a multiplatform artifact
since Compose Multiplatform 1.6.x. This provides `ViewModel`, `viewModelScope`, `ViewModelStore`,
`ViewModelStoreOwner`, and Compose integration officially. For **new projects**, there is no reason
to prefer kmp-viewmodel's core ViewModel over the official artifact.

The kmp-viewmodel ViewModel was always a stopgap — a reasonable one, but a stopgap.

### 2. Official SavedStateHandle multiplatform is emerging

The JetBrains lifecycle artifacts are progressively adding `SavedStateHandle` multiplatform
support. The timeline is uncertain (needs verification), but the direction is clear. Once
officially stable, the `viewmodel-savedstate` module's `SavedStateHandle` and `@Parcelize`
abstractions will also have official alternatives.

### 3. @Parcelize workaround complexity

Since Kotlin 2.0.0, using `@Parcelize` in `commonMain` requires adding:
```kotlin
freeCompilerArgs.addAll(
    "-P",
    "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.hoc081098.kmp.viewmodel.parcelable.Parcelize"
)
```
to every Android compilation in the multiplatform module. This is a build system footgun that
silently breaks if forgotten. It is documented but fragile.

### 4. Version coupling to AndroidX Lifecycle

On Android, kmp-viewmodel's `ViewModel` delegates to `androidx.lifecycle.ViewModel`. This means
the library must track AndroidX Lifecycle releases and update its dependency to avoid
incompatibilities. Every new AndroidX Lifecycle release that changes the ViewModel API requires
a kmp-viewmodel update, creating a dependency lag.

### 5. DI modules add version coupling chains

`kmp-viewmodel-koin-compose` depends on specific versions of Koin. Keeping these aligned with
user projects requires frequent minor releases. Each DI-specific module creates a separate
maintenance track.

---

## Relationship to Modern Official APIs

### JetBrains lifecycle-viewmodel multiplatform

As noted above, `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` now provides `ViewModel`,
`viewModelScope`, `ViewModelStore`, `ViewModelStoreOwner`, and Compose integration.

**Overlap:** The core `viewmodel` module of kmp-viewmodel is directly superseded by this artifact
on new projects.

**Residual value:** The type-safe `SavedStateHandle` key API (`NonNullSavedStateHandleKey`, `safe`
extension) and the Swift `Flow` wrappers are not part of the official artifacts and remain useful
regardless of which ViewModel base class is used.

### Kotlin Multiplatform @Parcelize

There is ongoing work in the Kotlin compiler to make `@Parcelize` work natively in
`commonMain` without workarounds. The kmp-viewmodel approach is a practical workaround for the
current compiler limitation, not a long-term solution.

### Compose `rememberViewModel` / `viewModel()` official

The official Compose multiplatform runtime ships `viewModel()` and related composable functions.
`kmpViewModel` in `viewmodel-compose` is a thin equivalent. The official functions will likely
provide a better integration path once fully stabilised.

---

## Does kmp-viewmodel Still Compile on Modern Kotlin / Compose?

**Yes.** Release 0.8.0 targets Kotlin 2.0.0 and Compose Multiplatform 1.6.11. No known
compilation failures at time of writing. The SNAPSHOT branch targets Kotlin 2.0.10.

---

## Unique Residual Value

Even after official APIs mature, the following parts of kmp-viewmodel retain independent value:

1. **Type-safe SavedStateHandle keys** — the `NonNullSavedStateHandleKey` / `safe` pattern is
   a good API design that does not depend on the ViewModel implementation. It can be extracted
   into a standalone utility or documented as a pattern.

2. **Swift Flow wrappers** — `NonNullStateFlowWrapper` and `NullableStateFlowWrapper` remain
   useful until Kotlin's Swift/Objective-C export of generics is completely production-ready
   across all use cases.

3. **Educational value** — the implementation of a multiplatform `ViewModel` using `expect`/`actual`
   is a clear, readable example of how to bridge Android framework types to KMP. The codebase is
   a good reference for library authors.

4. **The DI integration pattern** — `koinKmpViewModel` / `kojectKmpViewModel` demonstrate a
   clean pattern for composing DI factory resolution with Compose composition locals. The pattern
   itself transfers to any ViewModel base class.

---

## Verdict

kmp-viewmodel delivered genuine value when official multiplatform lifecycle/ViewModel APIs did not
exist. The core `ViewModel` module is now being superseded by official artifacts. The `savedstate`
module still provides useful utilities (type-safe keys, `@Parcelize` in common code) that have no
direct official equivalent yet. The Swift wrappers remain valuable for teams targeting iOS.

Recommendation: minimal maintenance mode for the core module; active documentation of the residual
useful patterns; planned deprecation notice once JetBrains lifecycle artifacts are fully stable.

See [maintenance/repository-status.md](../maintenance/repository-status.md) for the full
recommendation.
