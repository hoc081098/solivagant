package com.hoc081098.solivagant.sample.simple.ui.login

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlin.jvm.JvmField

@Parcelize
@Immutable
data object LoginScreenRoute : NavRoot

@JvmField
val LoginScreenDestination: NavDestination = ScreenDestination<LoginScreenRoute> { route, modifier ->
  LoginScreen(modifier = modifier, route = route)
}
