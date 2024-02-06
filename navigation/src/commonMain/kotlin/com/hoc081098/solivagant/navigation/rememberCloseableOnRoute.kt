package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.solivagant.navigation.internal.destinationId
import kotlin.reflect.KClass

/**
 * Remember a [Closeable] on the route.
 *
 * The [Closeable] will be created by [factory] once at the first access.
 * It will be closed when the [route] is removed from the back stack.
 */
@Composable
public fun <T : Closeable> rememberCloseableOnRoute(
  route: BaseRoute,
  type: KClass<T>,
  factory: () -> T,
): T {
  val executor = LocalNavigationExecutor.current
  val currentFactory by rememberUpdatedState(factory)

  return remember(executor, route) {
    // Don't use getOrCreate(key = type, factory = currentFactory) because it can refer to the old factory.
    // `{ currentFactory() }` is the right way.
    executor
      .storeFor(route.destinationId)
      .getOrCreate(key = type) { currentFactory() }
  }
}

/**
 * Remember a [Closeable] on the route.
 *
 * The [Closeable] will be created by [factory] once at the first access.
 * It will be closed when the [route] is removed from the back stack.
 */
@Composable
public inline fun <reified T : Closeable> rememberCloseableOnRoute(
  route: BaseRoute,
  noinline factory: @DisallowComposableCalls () -> T,
): T = rememberCloseableOnRoute(route, T::class, factory)
