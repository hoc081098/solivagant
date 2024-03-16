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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.InternalNavigationApi
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.internal.StackEntryState.ACTIVE
import com.hoc081098.solivagant.navigation.internal.StackEntryState.REMOVED
import com.hoc081098.solivagant.navigation.internal.StackEntryState.REMOVING
import dev.drewhamilton.poko.Poko
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Immutable
internal enum class StackEntryState {
  ACTIVE,
  REMOVING,
  REMOVED,
}

@Poko
@Stable
internal class StackEntry<T : BaseRoute>
  @InternalNavigationApi
  internal constructor(
    val id: StackEntryId,
    val route: T,
    val destination: ContentDestination<T>,
    val lifecycleOwner: StackEntryLifecycleOwner,
  ) {
    //region StackEntryState
    private val _state: MutableState<StackEntryState> = mutableStateOf(ACTIVE)

    internal fun transitionTo(state: StackEntryState) {
      Snapshot.withMutableSnapshot {
        _state.value = when (_state.value) {
          ACTIVE -> when (state) {
            ACTIVE -> error("The entry's state is already ACTIVE")
            REMOVING -> state
            REMOVED -> error("Cannot transition from ACTIVE to REMOVED")
          }

          REMOVING -> when (state) {
            ACTIVE -> error("Cannot transition from REMOVING to ACTIVE")
            REMOVING -> error("The entry's state is already REMOVING")
            REMOVED -> state
          }

          REMOVED -> error("REMOVED is the final state")
        }
      }
    }

    val state: State<StackEntryState> get() = _state
    //endregion

    val destinationId
      get() = route.destinationId

    val removable
      // cast is needed for the compiler to recognize that the when is exhaustive
      @Suppress("USELESS_CAST")
      get() = when (route as BaseRoute) {
        is NavRoute -> true
        is NavRoot -> false
      }

    //region Lifecycle
    internal fun moveToCreated() {
      lifecycleOwner.maxLifecycle = Lifecycle.State.CREATED
    }

    internal fun moveToStarted() {
      check(state.value == ACTIVE) { "Can not move to STARTED state when entry's state is not StackEntryState.ACTIVE" }
      lifecycleOwner.maxLifecycle = Lifecycle.State.STARTED
    }

    internal fun moveToResumed() {
      check(state.value == ACTIVE) { "Can not move to STARTED state when entry's state is not StackEntryState.ACTIVE" }
      lifecycleOwner.maxLifecycle = Lifecycle.State.RESUMED
    }

    internal fun moveToDestroyed() {
      lifecycleOwner.maxLifecycle = Lifecycle.State.DESTROYED
    }

    internal fun handleLifecycleEvent(event: Lifecycle.Event) = lifecycleOwner.handleLifecycleEvent(event)
    //endregion

    companion object {
      @OptIn(ExperimentalContracts::class)
      inline fun <T : BaseRoute> create(
        route: T,
        destinations: List<ContentDestination<*>>,
        hostLifecycleState: Lifecycle.State,
        idGenerator: () -> String,
      ): StackEntry<T> {
        contract {
          callsInPlace(idGenerator, InvocationKind.EXACTLY_ONCE)
        }

        @Suppress("UNCHECKED_CAST")
        val destination = destinations.find { it.id == route.destinationId } as? ContentDestination<T>
          ?: error(
            "Can not find ContentDestination for route $route." +
              "You must add a ContentDestination to the NavHost(destinations = ...).",
          )

        return StackEntry(
          id = StackEntryId(idGenerator()),
          route = route,
          destination = destination,
          lifecycleOwner = StackEntryLifecycleOwner(
            hostLifecycleState = hostLifecycleState,
          ),
        )
      }
    }
  }
