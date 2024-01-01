package com.hoc081098.solivagant.navigation.internal
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.benasher44.uuid.uuid4
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import kotlinx.collections.immutable.ImmutableList

internal class MultiStack(
  // Use ArrayList to make sure it is a RandomAccess
  private val allStacks: ArrayList<Stack>,
  private var startStack: Stack,
  private var currentStack: Stack,
  private val destinations: List<ContentDestination<*>>,
  private val onStackEntryRemoved: (StackEntry.Id) -> Unit,
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

  val startRoot = startStack.rootEntry.route as NavRoot

  fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T>? {
    val entry = currentStack.entryFor(destinationId)
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
    return allStacks.find { it.id == root.destinationId }
  }

  private fun createBackStack(root: NavRoot): Stack {
    val newStack = Stack.createWith(root, destinations, onStackEntryRemoved, idGenerator)
    allStacks.add(newStack)
    return newStack
  }

  private fun removeBackStack(stack: Stack) {
    stack.clear()
    allStacks.remove(stack)
    onStackEntryRemoved(stack.rootEntry.id)
  }

  private fun updateVisibleDestinations() {
    visibleEntryState.value = currentStack.computeVisibleEntries()
    canNavigateBackState.value = canNavigateBack()
  }

  private fun canNavigateBack(): Boolean {
    return currentStack.id != startStack.id || !currentStack.isAtRoot
  }

  fun push(route: NavRoute) {
    currentStack.push(route)
    updateVisibleDestinations()
  }

  fun popCurrentStack() {
    currentStack.pop()
    updateVisibleDestinations()
  }

  fun pop() {
    if (currentStack.isAtRoot) {
      check(currentStack.id != startStack.id) {
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
    updateVisibleDestinations()
  }

  fun <T : BaseRoute> popUpTo(
    destinationId: DestinationId<T>,
    isInclusive: Boolean,
  ) {
    currentStack.popUpTo(destinationId, isInclusive)
    updateVisibleDestinations()
  }

  fun push(root: NavRoot, clearTargetStack: Boolean) {
    val stack = getBackStack(root)
    currentStack = if (stack != null) {
      check(currentStack.id != stack.id) {
        "$root is already the current stack"
      }
      if (clearTargetStack) {
        removeBackStack(stack)
        createBackStack(root)
      } else {
        stack
      }
    } else {
      createBackStack(root)
    }
    if (stack?.id == startStack.id) {
      startStack = currentStack
    }
    updateVisibleDestinations()
  }

  fun resetToRoot(root: NavRoot) {
    if (root.destinationId == startStack.id) {
      if (currentStack.id != startStack.id) {
        removeBackStack(currentStack)
      }
      removeBackStack(startStack)
      val newStack = createBackStack(root)
      startStack = newStack
      currentStack = newStack
    } else if (root.destinationId == currentStack.id) {
      removeBackStack(currentStack)
      val newStack = createBackStack(root)
      currentStack = newStack
    } else {
      error("$root is not on the current back stack")
    }
    updateVisibleDestinations()
  }

  fun replaceAll(root: NavRoot) {
    // remove all stacks
    val iterator = allStacks.listIterator(allStacks.size)
    while (iterator.hasPrevious()) {
      val stack = iterator.previous()

      // Cannot use removeBackStack because we're modifying the list while iterating
      stack.clear()
      iterator.remove()
      onStackEntryRemoved(stack.rootEntry.id)
    }

    // create new stack with the root
    val newStack = createBackStack(root)
    startStack = newStack
    currentStack = newStack

    updateVisibleDestinations()
  }

  fun saveState(): Map<String, Any> {
    return mapOf(
      SAVED_STATE_ALL_STACKS to ArrayList(allStacks.map { it.saveState() }),
      SAVED_STATE_CURRENT_STACK to currentStack.id,
    )
  }

  companion object {
    fun createWith(
      root: NavRoot,
      destinations: List<ContentDestination<*>>,
      onStackEntryRemoved: (StackEntry.Id) -> Unit,
      idGenerator: () -> String = { uuid4().toString() },
    ): MultiStack {
      val startStack = Stack.createWith(root, destinations, onStackEntryRemoved, idGenerator)
      return MultiStack(
        allStacks = arrayListOf(startStack),
        startStack = startStack,
        currentStack = startStack,
        destinations = destinations,
        onStackEntryRemoved = onStackEntryRemoved,
        idGenerator = idGenerator,
      )
    }

    @Suppress("DEPRECATION")
    fun fromState(
      root: NavRoot,
      bundle: Map<String, Any?>,
      destinations: List<ContentDestination<*>>,
      onStackEntryRemoved: (StackEntry.Id) -> Unit,
      idGenerator: () -> String = { uuid4().toString() },
    ): MultiStack {
      @Suppress("UNCHECKED_CAST")
      val allStackBundles = bundle[SAVED_STATE_ALL_STACKS]!! as ArrayList<Map<String, ArrayList<out Any>>>
      val currentStackId = bundle[SAVED_STATE_CURRENT_STACK] as DestinationId<*>
      val allStacks = allStackBundles.mapTo(ArrayList(allStackBundles.size)) {
        Stack.fromState(it, destinations, onStackEntryRemoved, idGenerator)
      }
      val startStack = allStacks.first { it.id == root.destinationId }
      val currentStack = allStacks.first { it.id.route == currentStackId.route }
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
