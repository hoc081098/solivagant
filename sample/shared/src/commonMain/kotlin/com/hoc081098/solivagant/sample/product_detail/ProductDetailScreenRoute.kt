package com.hoc081098.solivagant.sample.product_detail

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Immutable
@Parcelize
data class ProductDetailScreenRoute(val id: Int) : NavRoute

@JvmField
val ProductDetailScreenDestination: NavDestination =
  ScreenDestination<ProductDetailScreenRoute> { ProductDetailScreen() }
