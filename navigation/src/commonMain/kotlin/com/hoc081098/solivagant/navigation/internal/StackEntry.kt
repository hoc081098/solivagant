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
