package com.hoc081098.solivagant.sample.wasm.second

import androidx.compose.material3.Text
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination

data object SecondScreenRoute: NavRoute

val SecondScreenDestination = ScreenDestination<SecondScreenRoute> { _, modifier ->
  Text("Second")
}
