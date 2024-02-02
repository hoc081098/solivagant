@file:Suppress("PackageNaming")

package com.hoc081098.solivagant.sample.simple.ui.home.notifications.nested_notifications

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Immutable
@Parcelize
data object NestedNotificationsScreenRoute : NavRoute

@JvmField
val NestedNotificationsScreenDestination =
  ScreenDestination<NestedNotificationsScreenRoute> { NestedNotificationsScreen() }
