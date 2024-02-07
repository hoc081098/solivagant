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

package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.solivagant.navigation.internal.DelicateNavigationApi
import kotlin.reflect.KClass

/**
 * Remember a [Closeable] on the route.
 *
 * The [Closeable] will be created by [factory] once at the first access.
 * It will be closed when the [route] is removed from the back stack.
 *
 * @param route The route to remember the [Closeable] on.
 * @param type The type of [Closeable].
 * @param factory The factory to create the [Closeable].
 */
@OptIn(DelicateNavigationApi::class)
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
      .storeFor(executor.stackEntryIdFor(route))
      .getOrCreate(key = type) { currentFactory() }
  }
}

/**
 * Remember a [Closeable] on the route.
 *
 * The [Closeable] will be created by [factory] once at the first access.
 * It will be closed when the [route] is removed from the back stack.
 *
 * @param route The route to remember the [Closeable] on.
 * @param factory The factory to create the [Closeable].
 */
@Composable
public inline fun <reified T : Closeable> rememberCloseableOnRoute(
  route: BaseRoute,
  noinline factory: () -> T,
): T = rememberCloseableOnRoute(route, T::class, factory)
