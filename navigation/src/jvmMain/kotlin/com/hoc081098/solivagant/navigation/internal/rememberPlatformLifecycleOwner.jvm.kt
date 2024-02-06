package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry

private class DefaultDesktopLifecycleOwner : LifecycleOwner {
  override val lifecycle: Lifecycle = LifecycleRegistry(initialState = Lifecycle.State.RESUMED)
}

@Composable
internal actual fun rememberPlatformLifecycleOwner(): LifecycleOwner {
  return remember { DefaultDesktopLifecycleOwner() }
}
