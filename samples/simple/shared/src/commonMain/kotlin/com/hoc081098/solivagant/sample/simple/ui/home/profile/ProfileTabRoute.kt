package com.hoc081098.solivagant.sample.simple.ui.home.profile

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Immutable
@Parcelize
data object ProfileTabRoute : NavRoot

@JvmField
val ProfileTabDestination = ScreenDestination<ProfileTabRoute> { ProfileTab() }
