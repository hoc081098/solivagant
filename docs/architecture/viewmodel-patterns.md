# ViewModel Patterns

Patterns from kmp-viewmodel worth preserving as architecture references.

---

## 1. Multiplatform ViewModel with Consistent viewModelScope

### What it is

`ViewModel` is declared as an `expect` class in `commonMain` with a concrete `actual`
implementation per target:

- **Android:** Delegates directly to `androidx.lifecycle.ViewModel`. The `viewModelScope` is
  `viewModel.viewModelScope` from AndroidX, bound to `Dispatchers.Main.immediate`.
- **Non-Android:** A pure Kotlin implementation. The `viewModelScope` is created lazily using
  the first available of `Dispatchers.Main.immediate` or `Dispatchers.Main`, cancelled in
  `onCleared()`.

The interface exposed to `commonMain` code is identical across all targets:

```kotlin
class SearchViewModel(
    private val repository: SearchRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    init {
        viewModelScope.launch {
            // Works on Android, iOS, Desktop, Web
        }
    }

    override fun onCleared() {
        // Called on all platforms when the ViewModel is cleared
    }
}
```

### Why it was interesting

Before official multiplatform ViewModel existed, Android developers writing KMP code had to choose
between:
1. Writing business logic without a lifecycle-aware scope (using global scopes or manual scope
   management).
2. Creating a custom `ScreenModel` abstraction (as in Voyager) that does not integrate with
   AndroidX on Android.
3. Forking the ViewModel concept per platform using `expect`/`actual`.

kmp-viewmodel chose option 3 with a clean public interface. The `actual` for Android is a
transparent delegate; teams do not give up AndroidX ViewModel semantics on Android.

### Current relevance

`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` now provides this officially. For new
projects, prefer the official artifact. The `expect`/`actual` ViewModel technique remains a
useful reference for library authors building multiplatform abstractions over Android-only APIs.

### Status

**Superseded for new projects by the official JetBrains lifecycle artifact. Remains an educational
reference for `expect`/`actual` design.**

---

## 2. Closeable Chain on ViewModel

### What it is

`ViewModel` accepts `Closeable` objects that are automatically closed before `onCleared()` is
called:

```kotlin
class MyViewModel(
    private val subscription: Closeable,  // e.g. a database connection or observer
) : ViewModel(subscription) {
    // subscription.close() is called automatically when this ViewModel is cleared
}
```

Resources can also be added after construction:

```kotlin
viewModel.addCloseable(myResource)
```

If `addCloseable` is called after the ViewModel is cleared, the `Closeable` is closed immediately.
This behaviour is consistent across all platforms.

### Why it was interesting

This mirrors the AndroidX ViewModel 2.5+ `addCloseable` API and makes it work consistently on
all targets. The pattern enables RAII-style resource management tied to ViewModel lifecycle without
subclassing `onCleared()` for every resource.

### Current relevance

The official multiplatform ViewModel artifact implements the same `addCloseable` / `Closeable` API.
The pattern itself (tying `Closeable` resources to ViewModel lifecycle) is now standard.

### Status

**Now standard. Pattern worth noting as a general technique for resource scoping.**

---

## 3. Type-Safe SavedStateHandle Keys

### What it is

Instead of accessing `SavedStateHandle` with raw `String` keys and untyped values, kmp-viewmodel
provides a type-safe key API:

```kotlin
// Define a key once with its type and default value
private val searchTermKey: NonNullSavedStateHandleKey<String> =
    NonNullSavedStateHandleKey.string(key = "search_term", defaultValue = "")

private val selectedPageKey: NullableSavedStateHandleKey<Int> =
    NullableSavedStateHandleKey.int(key = "selected_page")

// Access is type-safe — no casting
val searchTerm: String = savedStateHandle.safe[searchTermKey]
val page: Int? = savedStateHandle.safe[selectedPageKey]

// Observe as StateFlow — type is inferred
val searchTermFlow: StateFlow<String> = savedStateHandle.safe.getStateFlow(searchTermKey)
```

The `safe` extension returns a `SafeSavedStateHandle` accessor that accepts only
`NonNullSavedStateHandleKey<T>` or `NullableSavedStateHandleKey<T>` parameters.

### Why it was interesting

`SavedStateHandle` returns `T?` from `get(key)`, requiring the caller to know the correct type
and perform an unchecked cast. This is a footgun in AndroidX that this API eliminates. Key
definitions are centralized near the ViewModel that uses them, making the set of persisted state
explicit and readable.

The pattern also documents intent: `NonNullSavedStateHandleKey` signals that this key always
has a value; `NullableSavedStateHandleKey` signals that absence is meaningful.

### Current relevance

The official `SavedStateHandle` from AndroidX still uses raw `String` keys and `T?` returns.
The type-safe key pattern is not yet part of official APIs. This is a pattern worth extracting
as a reusable utility regardless of which ViewModel implementation is used.

**The pattern transfers to projects using the official JetBrains lifecycle artifacts with no
changes to the key definitions.**

### Status

**Still relevant. Not superseded. Worth extracting as a standalone utility or documenting as a
coding standard.**

---

## 4. ViewModelFactory / CreationExtras

### What it is

`ViewModelFactory<VM>` is a simple functional interface:

```kotlin
fun interface ViewModelFactory<VM : ViewModel> {
    fun create(extras: CreationExtras): VM
}
```

`CreationExtras` is a key-value map of construction parameters (similar to AndroidX
`CreationExtras`). Combining these:

```kotlin
val factory = ViewModelFactory<SearchViewModel> { extras ->
    SearchViewModel(
        savedStateHandle = extras.createSavedStateHandle(),
        repository = extras[MY_REPOSITORY_KEY]!!,
    )
}
```

In Compose:
```kotlin
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = kmpViewModel {
        SearchViewModel(
            savedStateHandle = createSavedStateHandle(),
            repository = get(),  // or inject from DI
        )
    }
)
```

### Why it was interesting

The `ViewModelFactory` / `CreationExtras` approach decouples ViewModel construction from the
DI container. The factory is a plain function. Testing ViewModels without a DI framework is
straightforward. DI frameworks (Koin, Koject) provide adapters that produce `ViewModelFactory`
instances.

### Current relevance

The official multiplatform lifecycle artifacts use the same `ViewModelFactory` and `CreationExtras`
design (kmp-viewmodel's design was modeled after it). The pattern is standard.

### Status

**Standard. No unique value beyond the official API. Good educational example of factory pattern
for dependency injection.**

---

## 5. Swift Flow Wrappers

### What it is

kmp-viewmodel provides wrapper classes that expose Kotlin `Flow` and `StateFlow` in a
Swift-friendly API:

```kotlin
// Kotlin common code
class SearchViewModel : ViewModel() {
    private val _results = MutableStateFlow<List<Product>>(emptyList())
    
    // Wrap for Swift consumption
    val results: NonNullStateFlowWrapper<List<Product>> = _results.wrap()
}
```

```swift
// Swift code
class IosViewModel: ObservableObject {
    @Published var results: [Product] = []
    private let vm: SearchViewModel
    
    init(vm: SearchViewModel) {
        self.vm = vm
        // Type-safe value access — no casting
        results = vm.results.value
        
        // subscribe(scope:onValue:) handles threading and cancellation
        vm.results.subscribe(scope: vm.viewModelScope) { [weak self] in
            self?.results = $0
        }
    }
    
    deinit { vm.clear() }
}
```

### Why it was interesting

Before Kotlin 1.9's improved Swift generics export, consuming `StateFlow<List<Product>>` in
Swift required writing `flow.collect { value in let typedValue = value as! [Product] }`. The
wrapper class eliminates the cast and provides a `subscribe` method with automatic scope cancellation.

The wrappers work even with older Kotlin/Native targets and do not require the Kotlin Coroutines
Swift extensions (`kotlinx-coroutines-core` alone is sufficient).

### Current relevance

As Kotlin's Swift export improves, these wrappers become less necessary. However, for teams
targeting older iOS versions or using Kotlin < 1.9.20, they remain valuable. The pattern of
providing a "Swift-shaped" API wrapper over Kotlin types is generally applicable.

### Status

**Partially relevant. Useful for teams with older Kotlin or specific Swift interop requirements.
Likely superseded by improved Kotlin/Swift generics export in Kotlin 2.x.**

---

## Summary Table

| Pattern | Still Relevant | Official API Equivalent | Unique to kmp-viewmodel |
|---------|---------------|------------------------|------------------------|
| Multiplatform ViewModel via `expect`/`actual` | ⚠️ Superseded | ✅ JetBrains lifecycle | No (now) |
| `Closeable` chain on ViewModel | ✅ Standard | ✅ JetBrains lifecycle | No |
| Type-safe `SavedStateHandle` keys | ✅ Yes | ❌ No | **Yes** |
| `ViewModelFactory` / `CreationExtras` | ✅ Standard | ✅ JetBrains lifecycle | No |
| Swift `Flow` wrappers | ⚠️ Partially | ❌ Partial | **Yes** |
| `@Parcelize` in `commonMain` | ✅ Yes (workaround) | ❌ Partial | **Yes** |
| DI adapter modules (Koin, Koject) | ✅ Yes | ❌ Not official | **Yes** |
