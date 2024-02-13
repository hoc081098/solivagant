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
import com.benasher44.uuid.uuid4
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.OverlayDestination
import com.hoc081098.solivagant.navigation.ScreenDestination
import dev.drewhamilton.poko.Poko
import kotlinx.collections.immutable.ImmutableList

@Stable
internal enum class StackEvent {
  Idle,
  Push,
  Pop,
  PushRoot,
  ReplaceAll,
}

@Immutable
@Poko
internal class VisibleEntryState(
  val visibleEntries: NonEmptyImmutableList<StackEntry<*>>,
  val previousVisibleEntry: StackEntry<*>?,
  val lastEvent: StackEvent,
) {
  inline val currentVisibleEntry get() = visibleEntries.first()
}

internal typealias OnStackEntryRemoved = (
  entry: StackEntry<*>,
  shouldRemoveImmediately: Boolean,
) -> Unit

private inline val StackEntry<*>.isOverlayDestination get() = destination is OverlayDestination<*>
private inline val StackEntry<*>.isScreenDestination get() = destination is ScreenDestination<*>

@Suppress("TooManyFunctions")
internal class MultiStack private constructor(
  // Use ArrayList to make sure it is a RandomAccess
  private val allStacks: ArrayList<Stack>,
  private var startStack: Stack,
  private var currentStack: Stack,
  private val destinations: List<ContentDestination<*>>,
  private val onStackEntryRemoved: OnStackEntryRemoved,
  private val idGenerator: () -> String,
) {
  private val visibleEntryState: MutableState<VisibleEntryState> =
    mutableStateOf(
      VisibleEntryState(
        visibleEntries = currentStack.computeVisibleEntries(),
        previousVisibleEntry = null,
        lastEvent = StackEvent.Idle,
      ),
    )
  val visibleEntries: State<VisibleEntryState>
    get() = visibleEntryState

  private val canNavigateBackState: MutableState<Boolean> =
    mutableStateOf(canNavigateBack())
  val canNavigateBack: State<Boolean>
    get() = canNavigateBackState

  val startRoot = startStack.rootEntry.route as NavRoot

  private var hostLifecycleState: Lifecycle.State = Lifecycle.State.INITIALIZED

  @Suppress("ReturnCount")
  fun <T : BaseRoute> entryFor(route: T): StackEntry<T>? {
    return allStacks.firstNotNullOfOrNull { it.entryFor(route) }
  }

  @Suppress("ReturnCount")
  fun <T : BaseRoute> entryFor(id: StackEntryId): StackEntry<T>? {
    return allStacks.firstNotNullOfOrNull { it.entryFor(id) }
  }

  @Suppress("ReturnCount")
  fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T>? {
    return allStacks.firstNotNullOfOrNull { it.entryFor(destinationId) }
  }

  private fun getBackStack(root: NavRoot): Stack? {
    return allStacks.find { it.destinationId == root.destinationId }
  }

  private fun createBackStack(root: NavRoot): Stack {
    val newStack = Stack.createWith(
      root = root,
      destinations = destinations,
      idGenerator = idGenerator,
      getHostLifecycleState = { hostLifecycleState },
    )
    allStacks.add(newStack)
    return newStack
  }

  private fun removeBackStack(stack: Stack, shouldRemoveImmediately: Boolean) {
    stack.clear().run {
      if (shouldRemoveImmediately) {
        onEach { onStackEntryRemoved(it, true) }
      } else {
        invokeOnStackEntryRemoved(onStackEntryRemoved)
      }
    }
    allStacks.remove(stack)
    onStackEntryRemoved(
      stack.rootEntry,
      shouldRemoveImmediately || stack.rootEntry.isOverlayDestination,
    )
  }

  private fun updateVisibleDestinations(lastEvent: StackEvent) {
    // Run this in a mutable snapshot (bit like a transaction)
    Snapshot.withMutableSnapshot {
      val previousVisibleEntry = visibleEntryState.value.currentVisibleEntry

      visibleEntryState.value = VisibleEntryState(
        visibleEntries = currentStack.computeVisibleEntries(),
        previousVisibleEntry = previousVisibleEntry,
        lastEvent = lastEvent,
      )
      canNavigateBackState.value = canNavigateBack()
    }
  }

  private fun canNavigateBack(): Boolean {
    return currentStack.destinationId != startStack.destinationId || !currentStack.isAtRoot
  }

  //region Stack operations
  fun push(route: NavRoute) {
    currentStack.push(route)
    updateVisibleDestinations(lastEvent = StackEvent.Push)
  }

  fun popCurrentStack() {
    currentStack.pop().also {
      onStackEntryRemoved(it, it.isOverlayDestination)
      updateVisibleDestinations(
        lastEvent = if (it.isOverlayDestination) {
          StackEvent.Idle
        } else {
          StackEvent.Pop
        },
      )
    }
  }

  fun pop() {
    if (currentStack.isAtRoot) {
      check(currentStack.destinationId != startStack.destinationId) {
        "Can't navigate back from the root of the start back stack"
      }
      removeBackStack(stack = currentStack, shouldRemoveImmediately = false)
      currentStack = startStack

      // remove anything that the start stack could have shown before
      // can't use resetToRoot because that will also recreate the root
      currentStack.clear().onEach { onStackEntryRemoved(it, true) }
      updateVisibleDestinations(lastEvent = StackEvent.Pop)
    } else {
      currentStack.pop().also {
        onStackEntryRemoved(it, it.isOverlayDestination)
        updateVisibleDestinations(
          lastEvent = if (it.isOverlayDestination) {
            StackEvent.Idle
          } else {
            StackEvent.Pop
          },
        )
      }
    }
  }

  fun <T : BaseRoute> popUpTo(
    destinationId: DestinationId<T>,
    isInclusive: Boolean,
  ) {
    val screenDestinationCount = currentStack
      .popUpTo(destinationId, isInclusive)
      .invokeOnStackEntryRemoved(onStackEntryRemoved)

    updateVisibleDestinations(
      lastEvent = if (screenDestinationCount > 0) {
        StackEvent.Pop
      } else {
        StackEvent.Idle
      },
    )
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

        removeBackStack(stack = stack, shouldRemoveImmediately = true)
        createBackStack(root)
      } else {
        lastEvent = if (currentStack == stack) {
          StackEvent.Idle
        } else {
          StackEvent.PushRoot
        }

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
          removeBackStack(stack = currentStack, shouldRemoveImmediately = false)
          removeBackStack(stack = startStack, shouldRemoveImmediately = true)
        } else {
          removeBackStack(stack = startStack, shouldRemoveImmediately = false)
        }
        val newStack = createBackStack(root)
        startStack = newStack
        currentStack = newStack

        updateVisibleDestinations(lastEvent = StackEvent.Pop)
      }

      currentStack.destinationId -> {
        removeBackStack(stack = currentStack, shouldRemoveImmediately = false)
        val newStack = createBackStack(root)
        currentStack = newStack

        updateVisibleDestinations(lastEvent = StackEvent.Pop)
      }

      else -> error("$root is not on the current back stack")
    }
  }

  fun replaceAll(root: NavRoot) {
    val currentVisibleEntry = visibleEntries.value.currentVisibleEntry

    // remove all stacks
    while (allStacks.isNotEmpty()) {
      val stack = allStacks.last()

      stack.clear().onEach { onStackEntryRemoved(it, it != currentVisibleEntry) }
      allStacks.removeLast()
      onStackEntryRemoved(
        stack.rootEntry,
        stack.rootEntry != currentVisibleEntry,
      )
    }

    // create new stack with the root
    val newStack = createBackStack(root)
    startStack = newStack
    currentStack = newStack

    updateVisibleDestinations(lastEvent = StackEvent.ReplaceAll)
  }
  //endregion

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
      onStackEntryRemoved: OnStackEntryRemoved,
      getHostLifecycleState: () -> Lifecycle.State,
      idGenerator: () -> String = { uuid4().toString() },
    ): MultiStack {
      val startStack = Stack.createWith(
        root = root,
        destinations = destinations,
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
      onStackEntryRemoved: OnStackEntryRemoved,
      idGenerator: () -> String = { uuid4().toString() },
    ): MultiStack {
      @Suppress("UNCHECKED_CAST")
      val allStackBundles = bundle[SAVED_STATE_ALL_STACKS]!! as ArrayList<Map<String, ArrayList<out Any>>>
      val currentStackId = bundle[SAVED_STATE_CURRENT_STACK] as DestinationId<*>
      val allStacks = allStackBundles.mapTo(ArrayList(allStackBundles.size)) {
        Stack.fromState(
          bundle = it,
          destinations = destinations,
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

/**
 * Invoke [onStackEntryRemoved] on each [StackEntry] in this list.
 * If the destination of the [StackEntry] is an [OverlayDestination], [onStackEntryRemoved] will be invoked with `true`.
 * Otherwise, [onStackEntryRemoved] will be invoked with `false` for the first [ScreenDestination]
 * and `true` for the rest.
 */
private fun ImmutableList<StackEntry<*>>.invokeOnStackEntryRemoved(onStackEntryRemoved: OnStackEntryRemoved): Int {
  var screenDestinationCount = 0
  var index: Int

  this
    .onEach {
      if (it.isScreenDestination) {
        screenDestinationCount++
      }
    }
    .also { index = screenDestinationCount }
    .forEach {
      if (it.isOverlayDestination) {
        onStackEntryRemoved(it, true)
      } else {
        // [3]--[2]--[1]
        // [3] -> false
        // [2] -> true
        // [1] -> true
        onStackEntryRemoved(it, index-- < screenDestinationCount)
      }
    }

  return screenDestinationCount
}
