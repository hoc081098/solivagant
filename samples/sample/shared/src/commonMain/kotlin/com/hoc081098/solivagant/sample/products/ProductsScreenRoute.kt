package com.hoc081098.solivagant.sample.products

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Immutable
@Parcelize
data object ProductsScreenRoute : NavRoute

@JvmField
val ProductsScreenDestination: NavDestination =
  ScreenDestination<ProductsScreenRoute> { ProductsScreen() }
