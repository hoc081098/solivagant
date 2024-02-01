package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.hoc081098.kmp.viewmodel.MainThread
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.ViewModelStore
import com.hoc081098.kmp.viewmodel.ViewModelStoreOwner
import kotlin.LazyThreadSafetyMode.NONE

@MainThread
public class SavedStateSupport :
  SavedStateHandleFactory,
  SaveableStateRegistry,
  ViewModelStoreOwner {
  private var registry: SaveableStateRegistry? = SaveableStateRegistry(
    restoredValues = emptyMap(),
    canBeSaved = { true },
  )

  private val savedStateHandle by lazy(NONE) { SavedStateHandle() }
  private val viewModelStoreLazy = lazy(NONE) { ViewModelStore() }
  override val viewModelStore: ViewModelStore by viewModelStoreLazy

  public fun clear() {
    if (registry == null) {
      // Already cleared
      return
    }

    registry = null
    if (viewModelStoreLazy.isInitialized()) {
      viewModelStore.clear()
    }
  }

  override fun create(): SavedStateHandle = savedStateHandle

  override fun canBeSaved(value: Any): Boolean = registry?.canBeSaved(value) == true

  override fun consumeRestored(key: String): Any? = registry?.consumeRestored(key)

  override fun performSave(): Map<String, List<Any?>> = registry
    ?.performSave()
    ?.also {
      registry = SaveableStateRegistry(
        restoredValues = it,
        canBeSaved = { true },
      )
    }
    .orEmpty()

  override fun registerProvider(key: String, valueProvider: () -> Any?): SaveableStateRegistry.Entry =
    registry
      ?.registerProvider(key, valueProvider)
      ?: NoOpSaveableStateRegistryEntry
}

private val NoOpSaveableStateRegistryEntry = object : SaveableStateRegistry.Entry {
  override fun unregister() {
    // No-op
  }
}
