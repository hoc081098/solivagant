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

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.InternalNavigationApi
import com.hoc081098.solivagant.navigation.Navigator
import com.hoc081098.solivagant.navigation.Serializable
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(
  level = RequiresOptIn.Level.WARNING,
  message = "This is a delicate API and its use requires care." +
    " Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.",
)
public annotation class DelicateNavigationApi

/**
 * A unique identifier for an entry in the navigation stack.
 */
@InternalNavigationApi
@JvmInline
public value class StackEntryId(public val value: String)

/**
 * An internal API for navigation.
 */
@InternalNavigationApi
public interface NavigationExecutor : Navigator {
  /**
   * Find the [StackEntryId] by the given [route].
   */
  @DelicateNavigationApi
  public fun stackEntryIdFor(route: BaseRoute): StackEntryId

  /**
   * Find the [StackEntryId] by the given [destinationId].
   */
  @DelicateNavigationApi
  @Deprecated("Should not use destinationId directly, use route instead.")
  public fun stackEntryIdFor(destinationId: DestinationId<*>): StackEntryId

  /**
   * Returns the [SavedStateHandle] for the given [id].
   */
  public fun savedStateHandleFor(id: StackEntryId): SavedStateHandle

  /**
   * Returns the [Store] for the given [id].
   */
  public fun storeFor(id: StackEntryId): Store

  /**
   * Returns the extra data for the given [id].
   */
  public fun extra(id: StackEntryId): Serializable

  @InternalNavigationApi
  public interface Store {
    public fun <T : Any> getOrCreate(
      key: KClass<T>,
      factory: () -> T,
    ): T
  }
}
