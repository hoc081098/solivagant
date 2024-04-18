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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import com.hoc081098.kmp.viewmodel.MainThread
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.safe.NullableSavedStateHandleKey
import com.hoc081098.kmp.viewmodel.safe.parcelable
import com.hoc081098.kmp.viewmodel.safe.safe
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import kotlin.jvm.JvmField

@MainThread
internal class LifecycleOwnerRef(
  @JvmField var ref: WeakReference<LifecycleOwner>?,
)

internal class StoreViewModel(
  internal val globalSavedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val executor: MutableState<MultiStackNavigationExecutor?> = mutableStateOf(null)
  private val lifecycleOwnerRef = LifecycleOwnerRef(null)

  init {
    addCloseable {
      lifecycleOwnerRef.ref?.clear()

      Snapshot.withMutableSnapshot {
        executor.value?.clear()
        executor.value = null
      }
    }
  }

  internal fun getMultiStackNavigationExecutor(
    startRoot: NavRoot,
    contentDestinations: List<ContentDestination<*>>,
    lifecycleOwner: LifecycleOwner,
  ): MultiStackNavigationExecutor {
    lifecycleOwnerRef.ref = WeakReference(lifecycleOwner)

    return Snapshot.withMutableSnapshot {
      val currentExecutor = executor.value

      val shouldClear = globalSavedStateHandle.safe { safeSavedStateHandle ->
        // Get saved input root
        val savedInputRoot = safeSavedStateHandle[SAVED_INPUT_ROOT_KEY]

        // Save new input root
        safeSavedStateHandle[SAVED_INPUT_ROOT_KEY] = startRoot

        when {
          // First time
          savedInputRoot == null -> false
          // Input root changed -> clear all state and recreate executor
          savedInputRoot != startRoot -> true
          // Do nothing
          else -> false
        }
      }

      if (shouldClear) {
        val newExecutor = MultiStackNavigationExecutor(
          contentDestinations = contentDestinations,
          lifecycleOwnerRef = lifecycleOwnerRef,
          globalSavedStateHandle = globalSavedStateHandle,
          scope = viewModelScope,
          startRoot = startRoot,
          restoreState = false,
        )

        // Update state before clearing
        executor.value = newExecutor
        currentExecutor?.clear()

        newExecutor
      } else {
        currentExecutor
          ?: MultiStackNavigationExecutor(
            contentDestinations = contentDestinations,
            lifecycleOwnerRef = lifecycleOwnerRef,
            globalSavedStateHandle = globalSavedStateHandle,
            scope = viewModelScope,
            startRoot = startRoot,
            restoreState = true,
          ).also { this.executor.value = it }
      }
    }
  }

  internal companion object {
    private val SAVED_INPUT_ROOT_KEY = NullableSavedStateHandleKey.parcelable<NavRoot>(
      "com.hoc081098.solivagant.navigation.store.input_root",
    )
  }
}
