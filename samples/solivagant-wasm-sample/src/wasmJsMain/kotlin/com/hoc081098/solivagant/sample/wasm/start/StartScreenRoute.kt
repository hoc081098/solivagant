package com.hoc081098.solivagant.sample.wasm.start

import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.ScreenDestination

data object StartScreenRoute : NavRoot

val StartScreenDestination = ScreenDestination<StartScreenRoute> { _, modifier ->
  StartScreen(
    modifier = modifier,
  )
}
