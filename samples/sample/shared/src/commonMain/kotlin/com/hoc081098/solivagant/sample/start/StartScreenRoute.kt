package com.hoc081098.solivagant.sample.start

import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Parcelize
data object StartScreenRoute : NavRoute, NavRoot

@JvmField
val StartScreenDestination: NavDestination =
  ScreenDestination<StartScreenRoute> { StartScreen() }
