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

package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hoc081098.kmp.viewmodel.InternalKmpViewModelApi
import com.hoc081098.kmp.viewmodel.compose.internal.kClassOf
import com.hoc081098.solivagant.navigation.internal.DestinationId

/**
 * A destination that can be navigated to. See `NavHost` for how to configure a `NavGraph` with it.
 */
public sealed interface NavDestination

@InternalNavigationApi
public sealed interface ContentDestination<T : BaseRoute> : NavDestination {
  @InternalNavigationApi
  public val id: DestinationId<T>

  @InternalNavigationApi
  public val extra: Serializable?

  @InternalNavigationApi
  public val content: @Composable (route: T, modifier: Modifier) -> Unit
}

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier. The given [content] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@OptIn(InternalKmpViewModelApi::class)
@Suppress("FunctionName")
public inline fun <reified T : BaseRoute> ScreenDestination(
  noinline content: @Composable (route: T, modifier: Modifier) -> Unit,
): NavDestination = ScreenDestination(DestinationId(kClassOf<T>()), null, content)

@OptIn(InternalKmpViewModelApi::class)
@InternalNavigationApi
@Suppress("FunctionName")
public inline fun <reified T : BaseRoute> ScreenDestination(
  extra: Serializable,
  noinline content: @Composable (route: T, modifier: Modifier) -> Unit,
): NavDestination = ScreenDestination(DestinationId(kClassOf<T>()), extra, content)

@InternalNavigationApi
public class ScreenDestination<T : BaseRoute>(
  override val id: DestinationId<T>,
  override val extra: Serializable?,
  override val content: @Composable (T, Modifier) -> Unit,
) : ContentDestination<T>

/**
 * Creates a new [NavDestination] that is shown on top a [ScreenDestination], for example a dialog or bottom sheet. The
 * class of [T] will be used as a unique identifier. The given [content] will be shown inside the dialog window when
 * navigating to it by using an instance of [T].
 */
@OptIn(InternalKmpViewModelApi::class)
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> OverlayDestination(
  noinline content: @Composable (route: T, modifier: Modifier) -> Unit,
): NavDestination = OverlayDestination(DestinationId(kClassOf<T>()), null, content)

@OptIn(InternalKmpViewModelApi::class)
@InternalNavigationApi
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> OverlayDestination(
  extra: Serializable,
  noinline content: @Composable (route: T, modifier: Modifier) -> Unit,
): NavDestination = OverlayDestination(DestinationId(kClassOf<T>()), extra, content)

@InternalNavigationApi
public class OverlayDestination<T : NavRoute>(
  override val id: DestinationId<T>,
  override val extra: Serializable?,
  override val content: @Composable (route: T, modifier: Modifier) -> Unit,
) : ContentDestination<T>
