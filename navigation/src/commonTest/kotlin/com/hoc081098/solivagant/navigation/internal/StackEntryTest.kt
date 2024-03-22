@file:Suppress("TestFunctionName")

package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.solivagant.navigation.ScreenDestination
import com.hoc081098.solivagant.navigation.test.SimpleRoot
import com.hoc081098.solivagant.navigation.test.SimpleRoute
import com.hoc081098.solivagant.navigation.test.simpleRootDestination
import com.hoc081098.solivagant.navigation.test.simpleRouteDestination
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import kotlin.test.Test

class StackEntryTest {
  private val lifecycleOwner = StackEntryLifecycleOwner()

  @Test
  fun StackEntry_hashCode_does_not_equal_others_with_the_different_id() {
    val first = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    val other = StackEntry(
      id = StackEntryId(value = "b"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    first.hashCode() shouldNotBeEqual other.hashCode()
  }

  @Test
  fun StackEntry_equals_does_not_equal_others_with_the_different_id() {
    val first = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    val other = StackEntry(
      id = StackEntryId(value = "b"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    first shouldNotBeEqual other
  }

  @Test
  fun StackEntry_equals_does_not_equal_others_with_the_different_route() {
    val first = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    val other = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoot(0),
      destination = simpleRootDestination,
      lifecycleOwner = lifecycleOwner,
    )
    first shouldNotBeEqual other
  }

  @Test
  fun StackEntry_equals_does_not_equal_others_with_the_different_destination() {
    val first = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    val other = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = ScreenDestination(DestinationId(SimpleRoute::class), null) { _, _ -> },
      lifecycleOwner = lifecycleOwner,
    )
    first shouldNotBeEqual other
  }

  @Test
  fun StackEntry_equals_does_not_equal_others_with_the_different_lifecycleOwner() {
    val first = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = StackEntryLifecycleOwner(),
    )
    val other = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    first shouldNotBeEqual other
  }

  @Test
  fun StackEntry_equals_to_others_with_the_same_id_route_destination_and_lifecycleOwner() {
    val first = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    val other = StackEntry(
      id = StackEntryId(value = "a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    first shouldBeEqual other
  }

  @Test
  fun StackEntry_destinationId_matches_destinations_id() {
    val entry = StackEntry(
      id = StackEntryId("a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    entry.destinationId shouldBeEqual simpleRouteDestination.id
  }

  @Test
  fun StackEntry_with_a_NavRoute_is_removable() {
    val entry = StackEntry(
      id = StackEntryId("a"),
      route = SimpleRoute(0),
      destination = simpleRouteDestination,
      lifecycleOwner = lifecycleOwner,
    )
    entry.removable.shouldBeTrue()
  }

  @Test
  fun StackEntry_with_a_NavRoot_is_not_removable() {
    val entry = StackEntry(
      id = StackEntryId("a"),
      route = SimpleRoot(0),
      destination = simpleRootDestination,
      lifecycleOwner = lifecycleOwner,
    )
    entry.removable.shouldBeFalse()
  }
}
