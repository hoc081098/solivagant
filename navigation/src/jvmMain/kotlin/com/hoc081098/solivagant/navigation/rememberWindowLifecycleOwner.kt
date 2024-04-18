package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.hoc081098.solivagant.lifecycle.LenientLifecycleRegistry
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.compose.rememberLifecycleOwner
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import java.awt.event.WindowListener

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@ExperimentalSolivagantApi
@Composable
public fun rememberWindowLifecycleOwner(): LifecycleOwner? {
  val window = androidx.compose.ui.window.LocalWindow.current
    ?: return null

  val lifecycleRegistry = remember(window) { LenientLifecycleRegistry() }

  DisposableEffect(window) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)

    val listener = object : WindowListener, WindowFocusListener {
      override fun windowOpened(event: WindowEvent?) = Unit

      override fun windowClosing(event: WindowEvent?) = Unit

      override fun windowClosed(event: WindowEvent?) = Unit

      override fun windowIconified(event: WindowEvent?) = lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)

      override fun windowDeiconified(event: WindowEvent?) {
        // The window is always in focus at this moment, so bump the state to [RESUMED].
        // It will generate [ON_START] event implicitly or skip it at all if [windowGainedFocus]
        // happened first.
        lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
      }

      override fun windowActivated(e: WindowEvent) = Unit

      override fun windowDeactivated(e: WindowEvent) = Unit

      //region WindowFocusListener
      override fun windowGainedFocus(event: WindowEvent) = onChangeWindowFocus()

      override fun windowLostFocus(event: WindowEvent) = onChangeWindowFocus()

      private fun onChangeWindowFocus() {
        lifecycleRegistry.onStateChanged(
          event = if (window.isFocused) {
            Lifecycle.Event.ON_RESUME
          } else {
            Lifecycle.Event.ON_PAUSE
          },
        )
      }
      //endregion
    }

    window.addWindowListener(listener)
    window.addWindowFocusListener(listener)

    onDispose {
      window.removeWindowListener(listener)
      window.removeWindowFocusListener(listener)

      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }
  }

  return rememberLifecycleOwner(lifecycleRegistry)
}
