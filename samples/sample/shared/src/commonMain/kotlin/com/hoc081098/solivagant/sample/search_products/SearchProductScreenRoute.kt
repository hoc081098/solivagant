@file:Suppress("PackageNaming")

package com.hoc081098.solivagant.sample.search_products

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Immutable
@Parcelize
data object SearchProductScreenRoute : NavRoute

@JvmField
val SearchProductScreenDestination: NavDestination =
  ScreenDestination<SearchProductScreenRoute> { _, modifier ->
    SearchProductsScreen(modifier = modifier)
  }
