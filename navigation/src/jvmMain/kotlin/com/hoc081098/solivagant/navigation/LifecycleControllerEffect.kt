/*
 * Copyright 2024 Petrus Nguyễn Thái Học
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.WindowState
import com.hoc081098.solivagant.lifecycle.LenientLifecycleRegistry
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleDestroyedException
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import com.hoc081098.solivagant.lifecycle.compose.rememberLifecycleOwner
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import java.awt.event.WindowListener
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
public fun LifecycleControllerEffect(
  lifecycleRegistry: LifecycleRegistry,
  windowState: WindowState,
) {
  val leavedComposition = remember(lifecycleRegistry) { MutableStateFlow(false) }

  LaunchedEffect(lifecycleRegistry, windowState) {
    // Cancel this effect when the composition leaves
    launch(start = CoroutineStart.UNDISPATCHED) {
      leavedComposition.first { it }
      this@LaunchedEffect.cancel()
    }

    // Wait the first state is not INITIALIZED
    when (lifecycleRegistry.currentStateFlow.first { it != Lifecycle.State.INITIALIZED }) {
      Lifecycle.State.DESTROYED ->
        throw LifecycleDestroyedException()

      Lifecycle.State.INITIALIZED ->
        error("Can not happen")

      Lifecycle.State.CREATED,
      Lifecycle.State.STARTED,
      Lifecycle.State.RESUMED,
      -> Unit
    }

    snapshotFlow { windowState.isMinimized }
      .collect { isMinimized ->
        if (
          lifecycleRegistry.currentState == Lifecycle.State.DESTROYED ||
          leavedComposition.value
        ) {
          throw LifecycleDestroyedException()
        }

        if (isMinimized) {
          moveToStopped(lifecycleRegistry)
        } else {
          moveToResumed(lifecycleRegistry)
        }
      }
  }

  DisposableEffect(lifecycleRegistry) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)

    onDispose {
      leavedComposition.value = true
      moveToDestroyed(lifecycleRegistry)
    }
  }
}

private fun moveToDestroyed(lifecycleRegistry: LifecycleRegistry) {
  when (lifecycleRegistry.currentState) {
    Lifecycle.State.INITIALIZED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }

    Lifecycle.State.CREATED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }

    Lifecycle.State.STARTED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }

    Lifecycle.State.RESUMED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_PAUSE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }

    Lifecycle.State.DESTROYED ->
      Unit
  }
}

private fun moveToResumed(lifecycleRegistry: LifecycleRegistry) {
  when (lifecycleRegistry.currentState) {
    Lifecycle.State.INITIALIZED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
    }

    Lifecycle.State.CREATED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
    }

    Lifecycle.State.STARTED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
    }

    Lifecycle.State.RESUMED ->
      Unit

    Lifecycle.State.DESTROYED ->
      throw LifecycleDestroyedException()
  }
}

private fun moveToStopped(lifecycleRegistry: LifecycleRegistry) {
  when (lifecycleRegistry.currentState) {
    Lifecycle.State.INITIALIZED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
    }

    Lifecycle.State.CREATED ->
      Unit

    Lifecycle.State.STARTED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
    }

    Lifecycle.State.RESUMED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_PAUSE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
    }

    Lifecycle.State.DESTROYED ->
      throw LifecycleDestroyedException()
  }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@Composable
public fun rememberWindowLifecycleOwner(): LifecycleOwner? {
  val window = androidx.compose.ui.window.LocalWindow.current
    ?: return null

  val lifecycleRegistry = remember(window) { LenientLifecycleRegistry() }

  DisposableEffect(window) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)

    val listener = object : WindowListener, WindowFocusListener {
      override fun windowOpened(event: WindowEvent?) {
        TODO("Not yet implemented")
      }

      override fun windowClosing(event: WindowEvent?) {
        TODO("Not yet implemented")
      }

      override fun windowClosed(event: WindowEvent?) {
        TODO("Not yet implemented")
      }

      override fun windowIconified(event: WindowEvent?) {
        TODO("Not yet implemented")
      }

      override fun windowDeiconified(event: WindowEvent?) {
        TODO("Not yet implemented")
      }

      override fun windowActivated(event: WindowEvent?) {
        TODO("Not yet implemented")
      }

      override fun windowDeactivated(event: WindowEvent?) {
        TODO("Not yet implemented")
      }

      //region WindowFocusListener
      override fun windowGainedFocus(event: WindowEvent?) {
        TODO("Not yet implemented")
      }

      override fun windowLostFocus(event: WindowEvent?) {
        TODO("Not yet implemented")
      }
      //endregion
    }

    window.addWindowListener(listener)
    window.addWindowFocusListener(listener)
    onDispose {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }
  }

  return rememberLifecycleOwner(lifecycleRegistry)
}
