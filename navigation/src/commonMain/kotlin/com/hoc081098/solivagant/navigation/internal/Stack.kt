package com.hoc081098.solivagant.navigation.internal

import com.benasher44.uuid.uuid4
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class Stack private constructor(
  initialStack: List<StackEntry<*>>,
  private val destinations: List<ContentDestination<*>>,
  private val onStackEntryRemoved: (StackEntry.Id) -> Unit,
  private val idGenerator: () -> String,
) {
  private val stack = ArrayDeque<StackEntry<*>>(20).also {
    it.addAll(initialStack)
  }

  val id: DestinationId<*> get() = rootEntry.destinationId
  val rootEntry: StackEntry<*> get() = stack.first()
  val isAtRoot: Boolean get() = !stack.last().removable

  @Suppress("UNCHECKED_CAST")
  fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T>? {
    return stack.findLast { it.destinationId == destinationId } as StackEntry<T>?
  }

  fun computeVisibleEntries(): ImmutableList<StackEntry<*>> {
    if (stack.size == 1) {
      return persistentListOf(stack.single())
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
        }.toImmutableList()
      }
    }

    error("Stack did not contain a ScreenDestination $stack")
  }

  fun push(route: NavRoute) {
    stack.add(entry(route, destinations, idGenerator))
  }

  fun pop() {
    check(stack.last().removable) { "Can't pop the root of the back stack" }
    popInternal()
  }

  private fun popInternal() {
    val entry = stack.removeLast()
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

  companion object {
    fun createWith(
      root: NavRoot,
      destinations: List<ContentDestination<*>>,
      onStackEntryRemoved: (StackEntry.Id) -> Unit,
      idGenerator: () -> String = { uuid4().toString() },
    ): Stack {
      val rootEntry = entry(root, destinations, idGenerator)
      return Stack(listOf(rootEntry), destinations, onStackEntryRemoved, idGenerator)
    }

    fun fromState(
      bundle: Map<String, ArrayList<out Any>>,
      destinations: List<ContentDestination<*>>,
      onStackEntryRemoved: (StackEntry.Id) -> Unit,
      idGenerator: () -> String = { uuid4().toString() },
    ): Stack {
      @Suppress("UNCHECKED_CAST")
      val ids = bundle[SAVED_STATE_IDS]!! as ArrayList<String>

      @Suppress("UNCHECKED_CAST")
      val routes = bundle[SAVED_STATE_ROUTES]!! as ArrayList<BaseRoute>
      val entries = ids.mapIndexed { index, id ->
        entry(routes[index], destinations) { id }
      }
      return Stack(entries, destinations, onStackEntryRemoved, idGenerator)
    }

    private inline fun <T : BaseRoute> entry(
      route: T,
      destinations: List<ContentDestination<*>>,
      idGenerator: () -> String,
    ): StackEntry<T> {
      @Suppress("UNCHECKED_CAST")
      val destination = destinations.find { it.id == route.destinationId } as ContentDestination<T>
      return StackEntry(StackEntry.Id(idGenerator()), route, destination)
    }

    private const val SAVED_STATE_IDS = "com.hoc081098.solivagant.navigation.stack.ids"
    private const val SAVED_STATE_ROUTES = "com.hoc081098.solivagant.navigation.stack.routes"
  }
}
