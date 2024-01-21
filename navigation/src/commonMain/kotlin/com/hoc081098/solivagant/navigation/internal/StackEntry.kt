package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Immutable
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import dev.drewhamilton.poko.Poko
import kotlin.jvm.JvmInline

@Poko
@Immutable
internal class StackEntry<T : BaseRoute> constructor(
  val id: Id,
  val route: T,
  val destination: ContentDestination<T>,
  lifecycleOwner: LifecycleOwner,
) : LifecycleOwner {
  private val lifecycleRegistry = LifecycleRegistry(Lifecycle.State.RESUMED)

  override val lifecycle: Lifecycle = MergedLifecycle(
    lifecycle2 = lifecycleOwner.lifecycle,
    lifecycle1 = lifecycleRegistry,
  )

  fun onStateChanged(event: Lifecycle.Event) = lifecycleRegistry.onStateChanged(event)

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
