package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Immutable
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.internal.destinationId
import dev.drewhamilton.poko.Poko
import kotlin.jvm.JvmInline

@Poko
@Immutable
internal class StackEntry<T : BaseRoute>(
  val id: Id,
  val route: T,
  val destination: ContentDestination<T>,
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

  @JvmInline
  value class Id(val value: String)
}
