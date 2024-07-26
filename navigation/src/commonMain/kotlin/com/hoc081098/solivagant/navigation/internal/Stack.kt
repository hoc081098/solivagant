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
import com.hoc081098.solivagant.navigation.StackValidationMode
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
  private val stackValidationMode: StackValidationMode,
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
  fun pop(): StackEntry<*>? {
    return when (stackValidationMode) {
      StackValidationMode.Lenient ->
        if (stack.last().removable) {
          popInternal(checkRemovable = false)
        } else {
          null
        }

      StackValidationMode.Strict ->
        popInternal(checkRemovable = true)

      is StackValidationMode.Warning ->
        if (stack.last().removable) {
          popInternal(checkRemovable = false)
        } else {
          stackValidationMode.logWarn(
            StackValidationMode.Warning.LOG_TAG,
            "Can't pop the root of the back stack",
          )
          null
        }
    }
  }

  /**
   * When [checkRemovable] is true, if the last entry is removable, it will be removed from the stack and returned.
   * Otherwise, a [IllegalStateException] or [NoSuchElementException] will be thrown.
   */
  @CheckResult(suggest = "")
  private fun popInternal(checkRemovable: Boolean): StackEntry<*> {
    if (checkRemovable) {
      check(stack.last().removable) { "Can't pop the root of the back stack" }
    }
    return stack.removeLast()
  }

  @CheckResult(suggest = "")
  fun popUpTo(
    destinationId: DestinationId<*>,
    isInclusive: Boolean,
  ): ImmutableList<StackEntry<*>> =
    persistentListOf<StackEntry<*>>().mutate { builder ->
      var earlyExit = false

      while (stack.last().destinationId != destinationId) {
        val isLastRemovable = stack.last().removable

        when (stackValidationMode) {
          StackValidationMode.Lenient -> {
            if (isLastRemovable) {
              // using popInternal with checkRemovable = false is enough here
              // because we know that the last entry is removable (see isLastRemovable).
              popInternal(checkRemovable = false).also(builder::add)
            } else {
              // if the last entry is not removable, we can't pop it, so we break the loop.
              earlyExit = true
              break
            }
          }

          StackValidationMode.Strict -> {
            check(isLastRemovable) { "Route ${destinationId.route} not found on back stack" }
            // using popInternal with checkRemovable = false is enough here
            // because we know that the last entry is removable (see above check).
            popInternal(checkRemovable = false).also(builder::add)
          }

          is StackValidationMode.Warning -> {
            if (isLastRemovable) {
              // using popInternal with checkRemovable = false is enough here
              // because we know that the last entry is removable (see isLastRemovable).
              popInternal(checkRemovable = false).also(builder::add)
            } else {
              stackValidationMode.logWarn(
                StackValidationMode.Warning.LOG_TAG,
                "Route ${destinationId.route} not found on back stack",
              )
              // if the last entry is not removable, we can't pop it, so we break the loop.
              earlyExit = true
              break
            }
          }
        }
      }

      if (earlyExit) {
        // if we break the loop early, that means destinationId is not found on the stack.
        // so we don't need to check isInclusive.
        return@mutate
      }

      if (isInclusive) {
        val isLastRemovable = stack.last().removable

        when (stackValidationMode) {
          StackValidationMode.Lenient -> {
            if (isLastRemovable) {
              // using popInternal with checkRemovable = false is enough here
              // because we know that the last entry is removable (see isLastRemovable).
              popInternal(checkRemovable = false).also(builder::add)
            }
          }

          StackValidationMode.Strict -> {
            // using popInternal with checkRemovable = true here to get the default removable check
            popInternal(checkRemovable = true).also(builder::add)
          }

          is StackValidationMode.Warning -> {
            if (isLastRemovable) {
              // using popInternal with checkRemovable = false is enough here
              // because we know that the last entry is removable (see isLastRemovable).
              popInternal(checkRemovable = false).also(builder::add)
            } else {
              stackValidationMode.logWarn(
                StackValidationMode.Warning.LOG_TAG,
                "Can't pop the root of the back stack",
              )
            }
          }
        }
      }
    }

  @CheckResult(suggest = "")
  fun clear(): ImmutableList<StackEntry<*>> =
    persistentListOf<StackEntry<*>>().mutate { builder ->
      while (stack.last().removable) {
        // using popInternal with checkRemovable = false is enough here
        // because we know that the last entry is removable (see while condition).
        popInternal(checkRemovable = false).also(builder::add)
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
      stackValidationMode: StackValidationMode,
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
        stackValidationMode = stackValidationMode,
      )
    }

    fun fromState(
      savedState: StackSavedState,
      destinations: List<ContentDestination<*>>,
      stackValidationMode: StackValidationMode,
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
        stackValidationMode = stackValidationMode,
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
