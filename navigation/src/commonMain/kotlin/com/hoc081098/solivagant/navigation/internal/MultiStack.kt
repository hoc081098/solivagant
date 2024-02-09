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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import com.benasher44.uuid.uuid4
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import kotlinx.collections.immutable.ImmutableList

@Stable
internal enum class StackEvent {
  Idle,
  Push,
  PushRoot,
  Pop,
  ReplaceAll,
}

@Suppress("TooManyFunctions")
internal class MultiStack(
  // Use ArrayList to make sure it is a RandomAccess
  private val allStacks: ArrayList<Stack>,
  private var startStack: Stack,
  private var currentStack: Stack,
  private val destinations: List<ContentDestination<*>>,
  private val onStackEntryRemoved: (StackEntryId) -> Unit,
  private val idGenerator: () -> String,
) {
  private val visibleEntryState: MutableState<ImmutableList<StackEntry<*>>> =
    mutableStateOf(currentStack.computeVisibleEntries())
  val visibleEntries: State<ImmutableList<StackEntry<*>>>
    get() = visibleEntryState

  private val canNavigateBackState: MutableState<Boolean> =
    mutableStateOf(canNavigateBack())
  val canNavigateBack: State<Boolean>
    get() = canNavigateBackState

  private val lastEventState: MutableState<StackEvent> = mutableStateOf(StackEvent.Idle)
  val lastEvent: State<StackEvent>
    get() = lastEventState

  val startRoot = startStack.rootEntry.route as NavRoot

  private var hostLifecycleState: Lifecycle.State = Lifecycle.State.INITIALIZED

  @Suppress("ReturnCount")
  fun <T : BaseRoute> entryFor(route: T): StackEntry<T>? {
    val entry = currentStack.entryFor<T>(route)
    if (entry != null) {
      return entry
    }

    // the root of the default back stack is always on the back stack
    if (startStack.rootEntry.route == route) {
      @Suppress("UNCHECKED_CAST")
      return startStack.rootEntry as StackEntry<T>
    }

    return null
  }

  @Suppress("ReturnCount")
  fun <T : BaseRoute> entryFor(id: StackEntryId): StackEntry<T>? {
    val entry = currentStack.entryFor<T>(id)
    if (entry != null) {
      return entry
    }

    // the root of the default back stack is always on the back stack
    if (startStack.rootEntry.id == id) {
      @Suppress("UNCHECKED_CAST")
      return startStack.rootEntry as StackEntry<T>
    }

    return null
  }

  @Suppress("ReturnCount")
  fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T>? {
    val entry = currentStack.entryFor<T>(destinationId)
    if (entry != null) {
      return entry
    }

    // the root of the default back stack is always on the back stack
    if (startStack.rootEntry.destinationId == destinationId) {
      @Suppress("UNCHECKED_CAST")
      return startStack.rootEntry as StackEntry<T>
    }

    return null
  }

  private fun getBackStack(root: NavRoot): Stack? {
    return allStacks.find { it.destinationId == root.destinationId }
  }

  private fun createBackStack(root: NavRoot): Stack {
    val newStack = Stack.createWith(
      root = root,
      destinations = destinations,
      onStackEntryRemoved = onStackEntryRemoved,
      idGenerator = idGenerator,
      getHostLifecycleState = { hostLifecycleState },
    )
    allStacks.add(newStack)
    return newStack
  }

  private fun removeBackStack(stack: Stack) {
    stack.clear()
    allStacks.remove(stack)
    onStackEntryRemoved(stack.rootEntry.id)
  }

  private fun updateVisibleDestinations(lastEvent: StackEvent) {
    Snapshot.withMutableSnapshot {
      lastEventState.value = lastEvent
      visibleEntryState.value = currentStack.computeVisibleEntries()
      canNavigateBackState.value = canNavigateBack()
    }
  }

  private fun canNavigateBack(): Boolean {
    return currentStack.destinationId != startStack.destinationId || !currentStack.isAtRoot
  }

  fun push(route: NavRoute) {
    currentStack.push(route)
    updateVisibleDestinations(lastEvent = StackEvent.Push)
  }

  fun popCurrentStack() {
    currentStack.pop()
    updateVisibleDestinations(lastEvent = StackEvent.Pop)
  }

  fun pop() {
    if (currentStack.isAtRoot) {
      check(currentStack.destinationId != startStack.destinationId) {
        "Can't navigate back from the root of the start back stack"
      }
      removeBackStack(currentStack)
      currentStack = startStack

      // remove anything that the start stack could have shown before
      // can't use resetToRoot because that will also recreate the root
      currentStack.clear()
    } else {
      currentStack.pop()
    }
    updateVisibleDestinations(lastEvent = StackEvent.Pop)
  }

  fun <T : BaseRoute> popUpTo(
    destinationId: DestinationId<T>,
    isInclusive: Boolean,
  ) {
    val isPopped = currentStack.popUpTo(destinationId, isInclusive)
    updateVisibleDestinations(lastEvent = if (isPopped) StackEvent.Pop else StackEvent.Idle)
  }

  fun push(root: NavRoot, clearTargetStack: Boolean) {
    val stack = getBackStack(root)
    val lastEvent: StackEvent

    currentStack = if (stack != null) {
      check(currentStack.destinationId != stack.destinationId) {
        "$root is already the current stack"
      }
      if (clearTargetStack) {
        lastEvent = StackEvent.PushRoot
        removeBackStack(stack)
        createBackStack(root)
      } else {
        lastEvent = StackEvent.Idle
        stack
      }
    } else {
      lastEvent = StackEvent.PushRoot
      createBackStack(root)
    }
    if (stack?.destinationId == startStack.destinationId) {
      startStack = currentStack
    }
    updateVisibleDestinations(lastEvent = lastEvent)
  }

  fun resetToRoot(root: NavRoot) {
    when (root.destinationId) {
      startStack.destinationId -> {
        if (currentStack.destinationId != startStack.destinationId) {
          removeBackStack(currentStack)
        }
        removeBackStack(startStack)
        val newStack = createBackStack(root)
        startStack = newStack
        currentStack = newStack
      }

      currentStack.destinationId -> {
        removeBackStack(currentStack)
        val newStack = createBackStack(root)
        currentStack = newStack
      }

      else -> error("$root is not on the current back stack")
    }
    updateVisibleDestinations(lastEvent = StackEvent.Pop)
  }

  fun replaceAll(root: NavRoot) {
    // remove all stacks
    while (allStacks.isNotEmpty()) {
      removeBackStack(allStacks.last())
    }

    // create new stack with the root
    val newStack = createBackStack(root)
    startStack = newStack
    currentStack = newStack

    updateVisibleDestinations(lastEvent = StackEvent.ReplaceAll)
  }

  fun saveState(): Map<String, Any> {
    return mapOf(
      SAVED_STATE_ALL_STACKS to ArrayList(allStacks.map { it.saveState() }),
      SAVED_STATE_CURRENT_STACK to currentStack.destinationId,
    )
  }

  fun handleLifecycleEvent(event: Lifecycle.Event) {
    hostLifecycleState = event.targetState
    allStacks.forEach { it.handleLifecycleEvent(event) }
  }

  companion object {
    fun createWith(
      root: NavRoot,
      destinations: List<ContentDestination<*>>,
      onStackEntryRemoved: (StackEntryId) -> Unit,
      getHostLifecycleState: () -> Lifecycle.State,
      idGenerator: () -> String = { uuid4().toString() },
    ): MultiStack {
      val startStack = Stack.createWith(
        root = root,
        destinations = destinations,
        onStackEntryRemoved = onStackEntryRemoved,
        getHostLifecycleState = getHostLifecycleState,
        idGenerator = idGenerator,
      )
      return MultiStack(
        allStacks = arrayListOf(startStack),
        startStack = startStack,
        currentStack = startStack,
        destinations = destinations,
        onStackEntryRemoved = onStackEntryRemoved,
        idGenerator = idGenerator,
      )
    }

    @Suppress("LongParameterList")
    fun fromState(
      root: NavRoot,
      bundle: Map<String, Any?>,
      destinations: List<ContentDestination<*>>,
      getHostLifecycleState: () -> Lifecycle.State,
      onStackEntryRemoved: (StackEntryId) -> Unit,
      idGenerator: () -> String = { uuid4().toString() },
    ): MultiStack {
      @Suppress("UNCHECKED_CAST")
      val allStackBundles = bundle[SAVED_STATE_ALL_STACKS]!! as ArrayList<Map<String, ArrayList<out Any>>>
      val currentStackId = bundle[SAVED_STATE_CURRENT_STACK] as DestinationId<*>
      val allStacks = allStackBundles.mapTo(ArrayList(allStackBundles.size)) {
        Stack.fromState(
          bundle = it,
          destinations = destinations,
          onStackEntryRemoved = onStackEntryRemoved,
          getHostLifecycleState = getHostLifecycleState,
          idGenerator = idGenerator,
        )
      }
      val startStack = allStacks.first { it.destinationId == root.destinationId }
      val currentStack = allStacks.first { it.destinationId.route == currentStackId.route }
      return MultiStack(
        allStacks = allStacks,
        startStack = startStack,
        currentStack = currentStack,
        destinations = destinations,
        onStackEntryRemoved = onStackEntryRemoved,
        idGenerator = idGenerator,
      )
    }

    private const val SAVED_STATE_ALL_STACKS = "com.hoc081098.solivagant.navigation.stack.all_stacks"
    private const val SAVED_STATE_CURRENT_STACK = "com.hoc081098.solivagant.navigation.stack.current_stack"
  }
}
