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

import com.hoc081098.kmp.viewmodel.parcelable.Parcelable
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.InternalNavigationApi
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.NavigationResultRequest
import dev.drewhamilton.poko.Poko

@InternalNavigationApi
public sealed interface NavEvent {

  @InternalNavigationApi
  @Poko
  public class NavigateToEvent(
    internal val route: NavRoute,
  ) : NavEvent

  @InternalNavigationApi
  @Poko
  public class NavigateToRootEvent(
    internal val root: NavRoot,
    internal val restoreRootState: Boolean,
  ) : NavEvent

  @InternalNavigationApi
  public data object UpEvent : NavEvent

  @InternalNavigationApi
  public data object BackEvent : NavEvent

  @InternalNavigationApi
  @Poko
  public class BackToEvent(
    internal val popUpTo: DestinationId<out BaseRoute>,
    internal val inclusive: Boolean,
  ) : NavEvent

  @InternalNavigationApi
  @Poko
  public class ResetToRoot(
    internal val root: NavRoot,
  ) : NavEvent

  @InternalNavigationApi
  @Poko
  public class ReplaceAll(
    internal val root: NavRoot,
  ) : NavEvent

  @InternalNavigationApi
  @Poko
  public class DestinationResultEvent<O : Parcelable>(
    internal val key: NavigationResultRequest.Key<O>,
    internal val result: O,
  ) : NavEvent

  @InternalNavigationApi
  @Poko
  public class MultiNavEvent(
    internal val navEvents: List<NavEvent>,
  ) : NavEvent
}
