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

import com.benasher44.uuid.uuid4
import com.hoc081098.kmp.viewmodel.parcelable.Parcelable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import dev.drewhamilton.poko.Poko
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

@Suppress("TooManyFunctions")
internal class Stack private constructor(
  initialStack: List<StackEntry<*>>,
  private val destinations: List<ContentDestination<*>>,
  private val getHostLifecycleState: () -> Lifecycle.State,
  private val idGenerator: () -> String,
) {
  @Suppress("MemberNameEqualsClassName")
  private val stack = ArrayDeque<StackEntry<*>>(@Suppress("MagicNumber") 20).also {
    it.addAll(initialStack)
  }

  val destinationId: DestinationId<*> get() = rootEntry.destinationId
  val rootEntry: StackEntry<*> get() = stack.first()
  val isAtRoot: Boolean get() = !stack.last().removable

  //region Entry lookup
  @Suppress("UNCHECKED_CAST")
  fun <T : BaseRoute> entryFor(id: StackEntryId): StackEntry<T>? {
    return stack.findLast { it.id == id } as StackEntry<T>?
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : BaseRoute> entryFor(route: T): StackEntry<T>? {
    return stack.findLast { it.route == route } as StackEntry<T>?
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T>? {
    return stack.findLast { it.destinationId == destinationId } as StackEntry<T>?
  }
  //endregion

  @CheckResult(suggest = "")
  @Suppress("NestedBlockDepth")
  internal fun computeVisibleEntries(): NonEmptyImmutableList<StackEntry<*>> {
    if (stack.size == 1) {
      return NonEmptyImmutableList.adapt(listOf(stack.single()))
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
        }.let { NonEmptyImmutableList.adapt(it) }
      }
    }

    error("Stack did not contain a ScreenDestination $stack")
  }

  //region Stack operations
  fun push(route: NavRoute) {
    val stackEntry = entry(
      route = route,
      destinations = destinations,
      hostLifecycleState = getHostLifecycleState(),
      idGenerator = idGenerator,
    ).apply { moveToCreated() }
    stack.add(stackEntry)
  }

  @CheckResult(suggest = "")
  fun pop(): StackEntry<*> {
    check(stack.last().removable) { "Can't pop the root of the back stack" }
    return popInternal()
  }

  @CheckResult(suggest = "")
  private fun popInternal(): StackEntry<*> = stack.removeLast()

  @CheckResult(suggest = "")
  fun popUpTo(
    destinationId: DestinationId<*>,
    isInclusive: Boolean,
  ): ImmutableList<StackEntry<*>> =
    persistentListOf<StackEntry<*>>().mutate { builder ->
      while (stack.last().destinationId != destinationId) {
        check(stack.last().removable) { "Route ${destinationId.route} not found on back stack" }
        popInternal().also(builder::add)
      }

      if (isInclusive) {
        // using pop here to get the default removable check
        pop().also(builder::add)
      }
    }

  @CheckResult(suggest = "")
  fun clear(): ImmutableList<StackEntry<*>> =
    persistentListOf<StackEntry<*>>().mutate { builder ->
      while (stack.last().removable) {
        popInternal()
          .also(builder::add)
      }
    }
  //endregion

  @CheckResult(suggest = "")
  internal fun saveState(): StackSavedState =
    StackSavedState(
      entries = stack.mapTo(ArrayList(stack.size)) {
        StackSavedState.Entry(
          id = it.id.value,
          route = it.route,
        )
      },
    )

  internal fun handleLifecycleEvent(event: Lifecycle.Event) = stack.forEach { it.handleLifecycleEvent(event) }

  companion object {
    fun createWith(
      root: NavRoot,
      destinations: List<ContentDestination<*>>,
      getHostLifecycleState: () -> Lifecycle.State,
      idGenerator: () -> String = { uuid4().toString() },
    ): Stack {
      val rootEntry = entry(
        route = root,
        destinations = destinations,
        idGenerator = idGenerator,
        hostLifecycleState = getHostLifecycleState(),
      )
      return Stack(
        initialStack = listOf(rootEntry),
        destinations = destinations,
        getHostLifecycleState = getHostLifecycleState,
        idGenerator = idGenerator,
      )
    }

    fun fromState(
      savedState: StackSavedState,
      destinations: List<ContentDestination<*>>,
      getHostLifecycleState: () -> Lifecycle.State,
      idGenerator: () -> String = { uuid4().toString() },
    ): Stack {
      val entries = savedState.entries.map { entry ->
        entry(
          route = entry.route,
          destinations = destinations,
          hostLifecycleState = getHostLifecycleState(),
        ) { entry.id }
      }
      return Stack(
        initialStack = entries,
        destinations = destinations,
        getHostLifecycleState = getHostLifecycleState,
        idGenerator = idGenerator,
      )
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun <T : BaseRoute> entry(
      route: T,
      destinations: List<ContentDestination<*>>,
      hostLifecycleState: Lifecycle.State,
      idGenerator: () -> String,
    ): StackEntry<T> {
      contract {
        callsInPlace(idGenerator, InvocationKind.EXACTLY_ONCE)
      }

      return StackEntry.create(
        route = route,
        destinations = destinations,
        idGenerator = idGenerator,
        hostLifecycleState = hostLifecycleState,
      )
    }
  }
}

@Poko
@Parcelize
internal class StackSavedState(val entries: ArrayList<Entry>) : Parcelable {
  @Poko
  @Parcelize
  internal class Entry(
    val id: String,
    val route: BaseRoute,
  ) : Parcelable
}
