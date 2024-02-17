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

import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.InternalNavigationApi
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.Navigator

internal class NavEventCollector : Navigator {
  private val _navEvents = mutableListOf<NavEvent>()
  internal val navEvents: List<NavEvent> = _navEvents

  override fun navigateTo(route: NavRoute) {
    val event = NavEvent.NavigateToEvent(route)
    _navEvents.add(event)
  }

  override fun navigateToRoot(
    root: NavRoot,
    restoreRootState: Boolean,
  ) {
    val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
    _navEvents.add(event)
  }

  override fun navigateUp() {
    val event = NavEvent.UpEvent
    _navEvents.add(event)
  }

  override fun navigateBack() {
    val event = NavEvent.BackEvent
    _navEvents.add(event)
  }

  @InternalNavigationApi
  override fun <T : BaseRoute> navigateBackToInternal(
    popUpTo: DestinationId<T>,
    inclusive: Boolean,
  ) {
    val event = NavEvent.BackToEvent(popUpTo, inclusive)
    _navEvents.add(event)
  }

  override fun resetToRoot(root: NavRoot) {
    val event = NavEvent.ResetToRoot(root)
    _navEvents.add(event)
  }

  override fun replaceAll(root: NavRoot) {
    val event = NavEvent.ReplaceAll(root)
    _navEvents.add(event)
  }
}
