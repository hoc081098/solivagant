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

package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.solivagant.navigation.InternalNavigationApi
import kotlin.reflect.KClass

@InternalNavigationApi
public class NavigationExecutorStore : NavigationExecutor.Store, Closeable {
  private val storedObjects = mutableMapOf<KClass<*>, Any>()

  override fun <T : Any> getOrCreate(
    key: KClass<T>,
    factory: () -> T,
  ): T {
    @Suppress("UNCHECKED_CAST")
    var storedObject = storedObjects[key] as T?
    if (storedObject == null) {
      storedObject = factory()
      storedObjects[key] = storedObject
    }
    return storedObject
  }

  override fun close() {
    storedObjects.forEach { (_, storedObject) ->
      if (storedObject is Closeable) {
        storedObject.close()
      }
    }
    storedObjects.clear()
  }
}
