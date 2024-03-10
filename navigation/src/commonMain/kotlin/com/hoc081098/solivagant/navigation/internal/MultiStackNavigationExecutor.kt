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
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.eventFlow
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.EXTRA_ROUTE
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.Serializable
import com.hoc081098.solivagant.navigation.internal.MultiStackNavigationExecutor.Companion.SAVED_STATE_STACK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
internal class MultiStackNavigationExecutor(
  /**
   * The list of content destinations.
   */
  contentDestinations: List<ContentDestination<*>>,
  /**
   * The lifecycle owner reference.
   */
  private val lifecycleOwnerRef: LifecycleOwnerRef,
  /**
   * The global saved state handle.
   */
  private val globalSavedStateHandle: SavedStateHandle,
  /**
   * This is used to create the stack when [restoreState] is `false`
   * or there is no saved state for the stack (associated with [SAVED_STATE_STACK] key).
   */
  private val startRoot: NavRoot,
  /**
   * Should restore the state of the stack from the saved state instead of creating a new one.
   */
  private val restoreState: Boolean,
  /**
   * The scope to launch the [lifecycleJob].
   * Do not cancel this scope, it is managed by the outside.
   */
  scope: CoroutineScope,
) : NavigationExecutor {
  private val stores = mutableMapOf<StackEntryId, NavigationExecutorStore>()
  private val savedStateHandles = mutableMapOf<StackEntryId, SavedStateHandle>()

  private val _lifecycleOwner = MutableStateFlow<LifecycleOwner?>(null)
  private val lifecycleJob: Job

  private val stack: MultiStack = createMultiStack(contentDestinations)

  val visibleEntries: State<VisibleEntryState>
    get() = stack.visibleEntries

  val canNavigateBack: State<Boolean>
    get() = stack.canNavigateBack

  /**
   * All entries that are removed from backstack, but not yet removed from the store.
   */
  private val pendingRemovedEntries = LinkedHashSet<StackEntry<*>>()

  init {
    lifecycleJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
      _lifecycleOwner
        .flatMapLatest { owner ->
          owner ?: return@flatMapLatest flowOf(null)

          owner
            .lifecycle
            .eventFlow
            .map { it to owner }
        }
        .collect(stack::handleLifecycleEvent)
    }

    globalSavedStateHandle.setSavedStateProviderWithParcelable(SAVED_STATE_STACK) { stack.saveState() }
  }

  //region NavigationExecutor
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
  }

  @DelicateNavigationApi
  override fun stackEntryIdFor(route: BaseRoute): StackEntryId = entryFor(route).id

  @DelicateNavigationApi
  @Deprecated("Should not use destinationId directly, use route instead.")
  override fun stackEntryIdFor(destinationId: DestinationId<*>): StackEntryId = entryFor(destinationId).id

  override fun savedStateHandleFor(id: StackEntryId): SavedStateHandle {
    val entry = entryFor<BaseRoute>(id)
    return savedStateHandles.getOrPut(id) {
      createSavedStateHandleAndSetSavedStateProvider(id.value, globalSavedStateHandle)
        .apply { this[EXTRA_ROUTE] = entry.route }
    }
  }

  override fun storeFor(id: StackEntryId): NavigationExecutor.Store {
    return stores.getOrPut(id) { NavigationExecutorStore() }
  }

  override fun extra(id: StackEntryId): Serializable {
    val entry = entryFor<BaseRoute>(id)
    return entry.destination.extra!!
  }
  //endregion

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

  /**
   * **Pre-condition**: the value of [StackEntry.isRemovedFromBackstack] must be `true`.
   *
   * Move the lifecycle of the entry to DESTROYED, and clear its resources.
   */
  private fun removeEntry(entry: StackEntry<*>) {
    require(entry.isRemovedFromBackstack.value) { "$entry is not removed from backstack" }

    val id = entry.id

    // If the entry is no longer part of the backStack, we need to manually move
    // it to DESTROYED, and clear its resources.
    entry.moveToDestroyed()

    val store = stores.remove(id)
    store?.close()

    savedStateHandles.remove(id)
    globalSavedStateHandle.removeSavedStateProvider(id.value)
    globalSavedStateHandle.remove<Any>(id.value)
  }

  /**
   * This method should be called by the [StoreViewModel].
   */
  internal fun clear() {
    stack.clearAll()
    removeAllPendingRemovedEntries()

    // Clear LifecycleOwner and cancel lifecycleJob
    setLifecycleOwner(null)
    lifecycleJob.cancel()

    // Remove saved state provider for the stack
    globalSavedStateHandle.removeSavedStateProvider(SAVED_STATE_STACK)

    // Close all stores
    for (store in stores.values) {
      store.close()
    }
    stores.clear()

    // Remove all SavedStateHandles
    for (key in savedStateHandles.keys) {
      globalSavedStateHandle.removeSavedStateProvider(key.value)
      globalSavedStateHandle.remove<Any>(key.value)
    }
    savedStateHandles.clear()
  }

  internal fun setLifecycleOwner(lifecycleOwner: LifecycleOwner?) {
    _lifecycleOwner.value = lifecycleOwner
  }

  /**
   * Remove all pending removed entries.
   */
  internal fun removeAllPendingRemovedEntries() {
    pendingRemovedEntries.forEach { removeEntry(it) }
    pendingRemovedEntries.clear()
  }

  /**
   * Remove the entry if it is removed from backstack, otherwise move its lifecycle to CREATED.
   */
  internal fun removeEntryIfNeeded(entry: StackEntry<*>) {
    if (entry.isRemovedFromBackstack.value) {
      removeEntry(entry)
      pendingRemovedEntries.remove(entry)
    } else {
      entry.moveToCreated()
    }
  }

  private fun createMultiStack(contentDestinations: List<ContentDestination<*>>): MultiStack {
    val savedState =
      if (restoreState) {
        globalSavedStateHandle
          .getParcelableFromSavedStateProvider(SAVED_STATE_STACK) as? MultiStackSavedState
      } else {
        null
      }

    val onStackEntryRemoved: OnStackEntryRemoved = { entry, shouldRemoveImmediately ->
      // First, mark the entry as removed from backstack
      entry.markRemovedFromBackstack()

      // Then, remove the entry if it is removed from backstack,
      // otherwise move its lifecycle to CREATED and add it to [pendingRemovedEntries].
      if (shouldRemoveImmediately) {
        removeEntry(entry)
        pendingRemovedEntries.remove(entry)
      } else {
        // The entry will be removed later by [removeEntry] or [removeAllPendingRemovedEntries].
        entry.moveToCreated()
        pendingRemovedEntries.add(entry)
      }
    }

    val getHostLifecycleState: () -> Lifecycle.State = fun(): Lifecycle.State {
      val owner = lifecycleOwnerRef.ref?.get()
        ?: // A LifecycleOwner is not required.
        // In the cases where one is not provided, always keep the host lifecycle at CREATED
        return Lifecycle.State.CREATED

      return owner.lifecycle.currentState
    }

    return if (savedState == null) {
      MultiStack.createWith(
        root = startRoot,
        destinations = contentDestinations,
        getHostLifecycleState = getHostLifecycleState,
        onStackEntryRemoved = onStackEntryRemoved,
      )
    } else {
      MultiStack.fromState(
        savedState = savedState,
        destinations = contentDestinations,
        getHostLifecycleState = getHostLifecycleState,
        onStackEntryRemoved = onStackEntryRemoved,
      )
    }
  }

  private companion object {
    private const val SAVED_STATE_STACK = "com.hoc081098.solivagant.navigation.stack"
  }
}
