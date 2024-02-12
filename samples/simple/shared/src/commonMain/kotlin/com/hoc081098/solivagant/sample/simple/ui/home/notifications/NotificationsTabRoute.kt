package com.hoc081098.solivagant.sample.simple.ui.home.notifications

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Immutable
@Parcelize
data object NotificationsTabRoute : NavRoot

@JvmField
val NotificationsTabDestination = ScreenDestination<NotificationsTabRoute> { route, modifier ->
  NotificationsTab(modifier = modifier)
}
