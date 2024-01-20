package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.WindowState
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry

@Composable
public fun LifecycleControllerEffect(lifecycleRegistry: LifecycleRegistry, windowState: WindowState) {
  LaunchedEffect(lifecycleRegistry, windowState) {
    snapshotFlow { windowState.isMinimized }
      .collect { isMinimized ->
        if (isMinimized) {
          lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
        } else {
          lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
        }
      }
  }

  DisposableEffect(lifecycleRegistry) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
    onDispose { lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY) }
  }
}

private class DefaultDesktopLifecycleOwner : LifecycleOwner {
  override val lifecycle: Lifecycle = LifecycleRegistry(initialState = Lifecycle.State.RESUMED)
}

@Composable
internal actual fun rememberPlatformLifecycleOwner(): LifecycleOwner {
  return remember { DefaultDesktopLifecycleOwner() }
}
