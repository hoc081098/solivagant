package com.hoc081098.solivagant.sample.simple.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import com.hoc081098.solivagant.navigation.rememberCloseableOnRoute
import com.hoc081098.solivagant.sample.simple.common.CloseableKoinScope
import kotlin.jvm.JvmField
import org.koin.compose.LocalKoinScope
import org.koin.compose.getKoin

@Immutable
@Parcelize
data class DetailScreenRoute(val id: String) : NavRoute

@JvmField
val DetailScreenDestination = ScreenDestination<DetailScreenRoute> { route, modifier ->
  val currentKoin by rememberUpdatedState(getKoin())

  val detailRouteScope = rememberCloseableOnRoute(route) {
    CloseableKoinScope(
      scope = currentKoin.createScope<DetailScreenRoute>(
        scopeId = route.toString(),
      ),
    )
  }.scope

  CompositionLocalProvider(LocalKoinScope provides detailRouteScope) {
    DetailScreen(modifier = modifier, route = route)
  }
}
