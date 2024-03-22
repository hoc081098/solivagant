package com.hoc081098.solivagant.navigation.test

import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.OverlayDestination
import com.hoc081098.solivagant.navigation.ScreenDestination
import com.hoc081098.solivagant.navigation.internal.DestinationId
import kotlin.jvm.JvmField
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@JvmField
internal val simpleRootDestination = ScreenDestination(DestinationId(SimpleRoot::class), null) { _, _ -> }

@JvmField
internal val otherRootDestination = ScreenDestination(DestinationId(OtherRoot::class), null) { _, _ -> }

@JvmField
internal val simpleRouteDestination = ScreenDestination(DestinationId(SimpleRoute::class), null) { _, _ -> }

@JvmField
internal val otherRouteDestination = OverlayDestination(DestinationId(OtherRoute::class), null) { _, _ -> }

@JvmField
internal val thirdRouteDestination = OverlayDestination(DestinationId(ThirdRoute::class), null) { _, _ -> }

@JvmField
internal val destinations: ImmutableList<ContentDestination<out BaseRoute>> = persistentListOf(
  simpleRootDestination,
  otherRootDestination,
  simpleRouteDestination,
  otherRouteDestination,
  thirdRouteDestination,
)
