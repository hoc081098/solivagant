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

import androidx.compose.runtime.State
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.eventFlow
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.Serializable
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
internal class MultiStackNavigationExecutor(
  private val stack: MultiStack,
  private val viewModel: StoreViewModel,
  private val onRootChanged: (NavRoot) -> Unit,
) : NavigationExecutor {
  private val _lifecycleOwner = MutableStateFlow<LifecycleOwner?>(null)

  val visibleEntries: State<VisibleEntryState>
    get() = stack.visibleEntries

  val canNavigateBack: State<Boolean>
    get() = stack.canNavigateBack

  init {
    viewModel.viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
      _lifecycleOwner
        .flatMapLatest { it?.lifecycle?.eventFlow ?: emptyFlow() }
        .collect(stack::handleLifecycleEvent)
    }
  }

  override fun navigateTo(route: NavRoute) {
    stack.push(route)
  }

  override fun navigateToRoot(
    root: NavRoot,
    restoreRootState: Boolean,
  ) {
    stack.push(root, clearTargetStack = !restoreRootState)
  }

  override fun navigateUp() {
    stack.popCurrentStack()
  }

  override fun navigateBack() {
    stack.pop()
  }

  override fun <T : BaseRoute> navigateBackToInternal(
    popUpTo: DestinationId<T>,
    inclusive: Boolean,
  ) {
    stack.popUpTo(popUpTo, inclusive)
  }

  override fun resetToRoot(root: NavRoot) {
    stack.resetToRoot(root)
  }

  override fun replaceAll(root: NavRoot) {
    stack.replaceAll(root)
    onRootChanged(root)
  }

  @DelicateNavigationApi
  override fun stackEntryIdFor(route: BaseRoute): StackEntryId = entryFor(route).id

  @DelicateNavigationApi
  @Deprecated("Should not use destinationId directly, use route instead.")
  override fun stackEntryIdFor(destinationId: DestinationId<*>): StackEntryId = entryFor(destinationId).id

  override fun savedStateHandleFor(id: StackEntryId): SavedStateHandle {
    val entry = entryFor<BaseRoute>(id)
    return viewModel.provideSavedStateHandle(entry.id, entry.route)
  }

  override fun storeFor(id: StackEntryId): NavigationExecutor.Store {
    val entry = entryFor<BaseRoute>(id)
    return viewModel.provideStore(entry.id)
  }

  override fun extra(id: StackEntryId): Serializable {
    val entry = entryFor<BaseRoute>(id)
    return entry.destination.extra!!
  }

  //region Find StackEntry by id or route or destinationId
  private fun <T : BaseRoute> entryFor(id: StackEntryId): StackEntry<T> {
    return stack.entryFor(id)
      ?: error("Route with id=$id not found on back stack")
  }

  private fun <T : BaseRoute> entryFor(route: T): StackEntry<T> {
    return stack.entryFor(route)
      ?: error("Route $route not found on back stack")
  }

  private fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T> {
    return stack.entryFor(destinationId)
      ?: error("Route with destinationId=$destinationId not found on back stack")
  }
  //endregion

  fun setLifecycleOwner(lifecycleOwner: LifecycleOwner?) {
    _lifecycleOwner.value = lifecycleOwner
  }

  /**
   * Remove all pending removed entries.
   */
  fun removeAllPendingRemovedEntries() = viewModel.removeAllPendingRemovedEntries()

  /**
   * Remove the entry if it is removed from backstack, otherwise move its lifecycle to CREATED.
   */
  fun removeEntryIfNeeded(entry: StackEntry<*>) = viewModel.removeEntryIfNeeded(entry)
}
