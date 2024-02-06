package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Immutable
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import dev.drewhamilton.poko.Poko

@Poko
@Immutable
internal class StackEntry<T : BaseRoute> private constructor(
  val id: StackEntryId,
  val route: T,
  val destination: ContentDestination<T>,
  val lifecycleOwner: StackEntryLifecycleOwner,
) {
  val destinationId
    get() = route.destinationId

  val removable
    // cast is needed for the compiler to recognize that the when is exhaustive
    @Suppress("USELESS_CAST")
    get() = when (route as BaseRoute) {
      is NavRoute -> true
      is NavRoot -> false
    }

  companion object {
    inline fun <T : BaseRoute> create(
      route: T,
      destinations: List<ContentDestination<*>>,
      hostLifecycleState: Lifecycle.State,
      idGenerator: () -> String,
    ): StackEntry<T> {
      @Suppress("UNCHECKED_CAST")
      val destination = destinations.find { it.id == route.destinationId } as? ContentDestination<T>
        ?: error(
          "Can not find ContentDestination for route $route." +
            "You must add a ContentDestination to the NavHost(destinations = ...).",
        )

      return StackEntry(
        id = StackEntryId(idGenerator()),
        route = route,
        destination = destination,
        lifecycleOwner = StackEntryLifecycleOwner(
          hostLifecycleState = hostLifecycleState,
        ),
      )
    }
  }
}
