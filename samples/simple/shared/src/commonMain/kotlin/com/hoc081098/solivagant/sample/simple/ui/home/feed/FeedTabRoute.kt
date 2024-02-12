package com.hoc081098.solivagant.sample.simple.ui.home.feed

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Immutable
@Parcelize
data object FeedTabRoute : NavRoot

@JvmField
val FeedTabDestination = ScreenDestination<FeedTabRoute> { route, modifier ->
  FeedTab(modifier = modifier)
}
