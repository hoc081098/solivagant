package com.hoc081098.solivagant.sample.simple.ui.detail.overlay

import androidx.compose.runtime.CompositionLocalProvider
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.OverlayDestination
import com.hoc081098.solivagant.sample.simple.ui.detail.DetailScreenRoute
import kotlin.jvm.JvmField
import org.koin.compose.LocalKoinScope
import org.koin.compose.getKoin

@Parcelize
data class DetailScreenOverlayRoute(
  val detailRoute: DetailScreenRoute,
  val id: String,
) : NavRoute

@JvmField
val DetailScreenOverlayDestination = OverlayDestination<DetailScreenOverlayRoute> { route, modifier ->
  val detailRouteScope = getKoin().getScope(scopeId = route.detailRoute.toString())

  CompositionLocalProvider(LocalKoinScope provides detailRouteScope) {
    DetailScreenOverlay(
      modifier = modifier,
      route = route,
    )
  }
}
