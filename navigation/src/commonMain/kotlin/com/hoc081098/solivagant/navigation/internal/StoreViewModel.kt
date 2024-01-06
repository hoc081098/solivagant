package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.EXTRA_ROUTE
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.internal.MultiStackNavigationExecutor.Companion.SAVED_STATE_STACK

internal class StoreViewModel(
  internal val globalSavedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val stores = mutableMapOf<StackEntry.Id, NavigationExecutorStore>()
  private val savedStateHandles = mutableMapOf<StackEntry.Id, SavedStateHandle>()
  private val savedStateHandleFactories = mutableMapOf<StackEntry.Id, SavedStateHandleFactory>()

  internal val savedNavRoot: NavRoot? get() = globalSavedStateHandle[SAVED_START_ROOT_KEY]

  internal fun provideStore(id: StackEntry.Id): NavigationExecutor.Store {
    return stores.getOrPut(id) { NavigationExecutorStore() }
  }

  internal fun provideSavedStateHandle(id: StackEntry.Id, route: BaseRoute): SavedStateHandle {
    return savedStateHandles.getOrPut(id) {
      createSavedStateHandleAndSetSavedStateProvider(id.value, globalSavedStateHandle)
        .apply { this[EXTRA_ROUTE] = route }
    }
  }

  internal fun provideSavedStateHandleFactory(id: StackEntry.Id, route: BaseRoute): SavedStateHandleFactory {
    return savedStateHandleFactories.getOrPut(id) {
      SavedStateHandleFactory { provideSavedStateHandle(id, route) }
    }
  }

  internal fun removeEntry(id: StackEntry.Id) {
    val store = stores.remove(id)
    store?.close()

    savedStateHandles.remove(id)
    savedStateHandleFactories.remove(id)
    globalSavedStateHandle.removeSavedStateProvider(id.value)
    globalSavedStateHandle.remove<Any>(id.value)
  }

  // @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  public override fun onCleared() {
    for (store in stores.values) {
      store.close()
    }
    stores.clear()

    for (key in savedStateHandles.keys) {
      globalSavedStateHandle.removeSavedStateProvider(key.value)
      globalSavedStateHandle.remove<Any>(key.value)
    }
    savedStateHandles.clear()
    savedStateHandleFactories.clear()
  }

  internal fun setInputStartRoot(root: NavRoot) {
    globalSavedStateHandle.get<NavRoot?>(SAVED_INPUT_START_ROOT_KEY).let { currentSavedInputStartRoot ->
      when {
        currentSavedInputStartRoot == null -> {
          globalSavedStateHandle[SAVED_INPUT_START_ROOT_KEY] = root
          globalSavedStateHandle[SAVED_START_ROOT_KEY] = root
        }

        currentSavedInputStartRoot != root -> {
          // Clear all saved state
          onCleared()

          globalSavedStateHandle[SAVED_INPUT_START_ROOT_KEY] = root
          globalSavedStateHandle[SAVED_START_ROOT_KEY] = root
        }

        else -> {
          // Do nothing
        }
      }
    }
  }

  internal fun setStartRoot(root: NavRoot) {
    globalSavedStateHandle[SAVED_START_ROOT_KEY] = root
  }

  internal fun getSavedStackState(): Map<String, Any?>? =
    globalSavedStateHandle.getAsMap(SAVED_STATE_STACK)

  private companion object {
    private const val SAVED_START_ROOT_KEY = "com.hoc081098.solivagant.navigation.store.start_root"
    private const val SAVED_INPUT_START_ROOT_KEY = "com.hoc081098.solivagant.navigation.store.input_start_root"
  }
}
