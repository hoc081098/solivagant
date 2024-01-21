package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner as AndroidXLocalLifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.asSolivagantLifecycleOwner

@Composable
internal actual fun rememberPlatformLifecycleOwner(): LifecycleOwner {
  val androidXLifecycleOwner = AndroidXLocalLifecycleOwner.current
  return remember(androidXLifecycleOwner) { androidXLifecycleOwner.asSolivagantLifecycleOwner() }
}
