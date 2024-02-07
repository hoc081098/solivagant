/*
 * Copyright 2021 Freeletics GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2024 Petrus Nguyễn Thái Học
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.EXTRA_ROUTE
import com.hoc081098.solivagant.navigation.NavRoot

internal class StoreViewModel(
  internal val globalSavedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val stores = mutableMapOf<StackEntryId, NavigationExecutorStore>()
  private val savedStateHandles = mutableMapOf<StackEntryId, SavedStateHandle>()
  private val savedStateHandleFactories = mutableMapOf<StackEntryId, SavedStateHandleFactory>()

  private var stack: MultiStack? = null
  private var executor: MultiStackNavigationExecutor? = null

  init {
    globalSavedStateHandle.setSavedStateProviderWithMap(SAVED_STATE_STACK) {
      checkNotNull(stack) { "Stack is null. This should never happen" }.saveState()
    }
  }

  internal fun provideStore(id: StackEntryId): NavigationExecutor.Store {
    return stores.getOrPut(id) { NavigationExecutorStore() }
  }

  internal fun provideSavedStateHandle(id: StackEntryId, route: BaseRoute): SavedStateHandle {
    return savedStateHandles.getOrPut(id) {
      createSavedStateHandleAndSetSavedStateProvider(id.value, globalSavedStateHandle)
        .apply { this[EXTRA_ROUTE] = route }
    }
  }

  internal fun provideSavedStateHandleFactory(id: StackEntryId, route: BaseRoute): SavedStateHandleFactory {
    return savedStateHandleFactories.getOrPut(id) {
      SavedStateHandleFactory { provideSavedStateHandle(id, route) }
    }
  }

  private fun removeEntry(id: StackEntryId) {
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

  private fun setStartRoot(root: NavRoot) {
    globalSavedStateHandle[SAVED_START_ROOT_KEY] = root
  }

  fun getMultiStackNavigationExecutor(
    contentDestinations: List<ContentDestination<*>>,
    getHostLifecycleState: () -> Lifecycle.State,
  ): MultiStackNavigationExecutor =
    executor
      ?: MultiStackNavigationExecutor(
        stack = getMultiStack(contentDestinations, getHostLifecycleState),
        viewModel = this,
        onRootChanged = ::setStartRoot,
      ).also { this.executor = it }

  private fun getMultiStack(
    contentDestinations: List<ContentDestination<*>>,
    getHostLifecycleState: () -> Lifecycle.State,
  ): MultiStack {
    this.stack?.let { return it }

    val navState = globalSavedStateHandle.getAsMap(SAVED_STATE_STACK)
    val savedNavRoot: NavRoot? = globalSavedStateHandle[SAVED_START_ROOT_KEY]
    return if (navState == null) {
      MultiStack.createWith(
        root = savedNavRoot!!,
        destinations = contentDestinations,
        getHostLifecycleState = getHostLifecycleState,
        onStackEntryRemoved = ::removeEntry,
      )
    } else {
      MultiStack.fromState(
        root = savedNavRoot!!,
        bundle = navState,
        destinations = contentDestinations,
        getHostLifecycleState = getHostLifecycleState,
        onStackEntryRemoved = ::removeEntry,
      )
    }.also { this.stack = it }
  }

  private companion object {
    private const val SAVED_STATE_STACK = "com.hoc081098.solivagant.navigation.stack"
    private const val SAVED_START_ROOT_KEY = "com.hoc081098.solivagant.navigation.store.start_root"
    private const val SAVED_INPUT_START_ROOT_KEY = "com.hoc081098.solivagant.navigation.store.input_start_root"
  }
}
