package com.hoc081098.solivagant.sample.simple.ui.detail

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Immutable
@Parcelize
data class DetailScreenRoute(val id: String) : NavRoute

@JvmField
val DetailScreenDestination = ScreenDestination<DetailScreenRoute> { DetailScreen(route = it) }
