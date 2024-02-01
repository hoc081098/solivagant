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
  private var registry = SaveableStateRegistry(
    restoredValues = emptyMap(),
    canBeSaved = { true },
  )

  private val viewModelStoreLazy = lazy(NONE) { ViewModelStore() }
  private val savedStateHandle by lazy(NONE) { SavedStateHandle() }
  private var isCleared = false

  override val viewModelStore: ViewModelStore
    get() {
      check(!isCleared) { "Cannot access ViewModelStore after it's cleared" }
      return viewModelStoreLazy.value
    }

  public fun clear() {
    if (isCleared) {
      return
    }
    isCleared = true
    if (viewModelStoreLazy.isInitialized()) {
      viewModelStore.clear()
    }
  }

  override fun create(): SavedStateHandle {
    check(!isCleared) { "Cannot create SavedStateHandle after it's cleared" }
    return savedStateHandle
  }

  override fun canBeSaved(value: Any): Boolean = registry.canBeSaved(value)

  override fun consumeRestored(key: String): Any? = registry.consumeRestored(key)

  override fun performSave(): Map<String, List<Any?>> = registry
    .performSave()
    .also {
      registry = SaveableStateRegistry(
        restoredValues = it,
        canBeSaved = { true },
      )
    }

  override fun registerProvider(key: String, valueProvider: () -> Any?): SaveableStateRegistry.Entry =
    registry.registerProvider(key, valueProvider)
}
