package com.hoc081098.solivagant.navigation.internal

import com.benasher44.uuid.uuid4
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.adapters.ImmutableListAdapter

internal class Stack private constructor(
  initialStack: List<StackEntry<*>>,
  private val destinations: List<ContentDestination<*>>,
  private val onStackEntryRemoved: (StackEntry.Id) -> Unit,
  private val idGenerator: () -> String,
) {
  @Suppress("MemberNameEqualsClassName")
  private val stack = ArrayDeque<StackEntry<*>>(@Suppress("MagicNumber") 20).also {
    it.addAll(initialStack)
  }
  internal var hostLifecycleState: Lifecycle.State = Lifecycle.State.INITIALIZED

  val id: DestinationId<*> get() = rootEntry.destinationId
  val rootEntry: StackEntry<*> get() = stack.first()
  val isAtRoot: Boolean get() = !stack.last().removable

  @Suppress("UNCHECKED_CAST")
  fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T>? {
    return stack.findLast { it.destinationId == destinationId } as StackEntry<T>?
  }

  @Suppress("NestedBlockDepth")
  fun computeVisibleEntries(): ImmutableList<StackEntry<*>> {
    if (stack.size == 1) {
      return ImmutableListAdapter(listOf(stack.single()))
    }

    // go through the stack from the top until reaching the first ScreenDestination
    // then create a List of the elements starting from there
    val iterator = stack.listIterator(stack.size)
    while (iterator.hasPrevious()) {
      if (iterator.previous().destination is ScreenDestination<*>) {
        val expectedSize = stack.size - iterator.nextIndex()
        return ArrayList<StackEntry<*>>(expectedSize).apply {
          while (iterator.hasNext()) {
            add(iterator.next())
          }
        }.let(::ImmutableListAdapter)
      }
    }

    error("Stack did not contain a ScreenDestination $stack")
  }

  fun push(route: NavRoute) {
    stack.add(
      entry(
        route = route,
        destinations = destinations,
        hostLifecycleState = hostLifecycleState,
        idGenerator = idGenerator,
      ).apply {
        lifecycleOwner.maxLifecycle = Lifecycle.State.CREATED
      },
    )
  }

  fun pop() {
    check(stack.last().removable) { "Can't pop the root of the back stack" }
    popInternal()
  }

  private fun popInternal() {
    val entry = stack.removeLast()
    entry.lifecycleOwner.maxLifecycle = Lifecycle.State.DESTROYED
    onStackEntryRemoved(entry.id)
  }

  fun popUpTo(destinationId: DestinationId<*>, isInclusive: Boolean) {
    while (stack.last().destinationId != destinationId) {
      check(stack.last().removable) { "Route ${destinationId.route} not found on back stack" }
      popInternal()
    }

    if (isInclusive) {
      // using pop here to get the default removable check
      pop()
    }
  }

  fun clear() {
    while (stack.last().removable) {
      popInternal()
    }
  }

  fun saveState(): Map<String, ArrayList<out Any>> {
    val ids = ArrayList<String>(stack.size)
    val routes = ArrayList<BaseRoute>(stack.size)
    stack.forEach {
      ids.add(it.id.value)
      routes.add(it.route)
    }
    return mapOf(
      SAVED_STATE_IDS to ids,
      SAVED_STATE_ROUTES to routes,
    )
  }

  internal fun handleLifecycleEvent(event: Lifecycle.Event) {
    println("$this handleLifecycleEvent $event")
    hostLifecycleState = event.targetState
    stack.forEach { it.lifecycleOwner.handleLifecycleEvent(event) }
    stack.forEach { println(">>>  ${it.route} ${it.lifecycleOwner}") }
  }

  companion object {
    fun createWith(
      root: NavRoot,
      destinations: List<ContentDestination<*>>,
      hostLifecycleState: Lifecycle.State,
      onStackEntryRemoved: (StackEntry.Id) -> Unit,
      idGenerator: () -> String = { uuid4().toString() },
    ): Stack {
      val rootEntry = entry(
        route = root,
        destinations = destinations,
        idGenerator = idGenerator,
        hostLifecycleState = hostLifecycleState,
      )
      return Stack(
        initialStack = listOf(rootEntry),
        destinations = destinations,
        onStackEntryRemoved = onStackEntryRemoved,
        idGenerator = idGenerator,
      )
    }

    fun fromState(
      bundle: Map<String, ArrayList<out Any>>,
      destinations: List<ContentDestination<*>>,
      hostLifecycleState: Lifecycle.State,
      onStackEntryRemoved: (StackEntry.Id) -> Unit,
      idGenerator: () -> String = { uuid4().toString() },
    ): Stack {
      @Suppress("UNCHECKED_CAST")
      val ids = bundle[SAVED_STATE_IDS]!! as ArrayList<String>

      @Suppress("UNCHECKED_CAST")
      val routes = bundle[SAVED_STATE_ROUTES]!! as ArrayList<BaseRoute>
      val entries = ids.mapIndexed { index, id ->
        entry(
          route = routes[index],
          destinations = destinations,
          hostLifecycleState = hostLifecycleState,
        ) { id }
      }
      return Stack(
        initialStack = entries,
        destinations = destinations,
        onStackEntryRemoved = onStackEntryRemoved,
        idGenerator = idGenerator,
      )
    }

    private inline fun <T : BaseRoute> entry(
      route: T,
      destinations: List<ContentDestination<*>>,
      hostLifecycleState: Lifecycle.State,
      idGenerator: () -> String,
    ): StackEntry<T> =
      StackEntry.create(
        route = route,
        destinations = destinations,
        idGenerator = idGenerator,
        hostLifecycleState = hostLifecycleState,
      )

    private const val SAVED_STATE_IDS = "com.hoc081098.solivagant.navigation.stack.ids"
    private const val SAVED_STATE_ROUTES = "com.hoc081098.solivagant.navigation.stack.routes"
  }
}
