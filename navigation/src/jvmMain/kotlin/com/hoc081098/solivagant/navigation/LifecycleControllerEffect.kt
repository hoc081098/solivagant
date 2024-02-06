package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.WindowState
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private class DestroyedLifecycleException : CancellationException("Lifecycle was destroyed")

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
        throw DestroyedLifecycleException()

      Lifecycle.State.INITIALIZED ->
        error("Can not happen")

      Lifecycle.State.CREATED,
      Lifecycle.State.STARTED,
      Lifecycle.State.RESUMED,
      ->
        Unit
    }

    snapshotFlow { windowState.isMinimized }
      .collect { isMinimized ->
        if (
          lifecycleRegistry.currentState == Lifecycle.State.DESTROYED ||
          leavedComposition.value
        ) {
          throw DestroyedLifecycleException()
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
      throw DestroyedLifecycleException()
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
      throw DestroyedLifecycleException()
  }
}
