package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import dev.drewhamilton.poko.Poko

@Poko
internal class StackEntryLifecycleOwner(
  private var hostLifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
) : LifecycleOwner {
  internal var maxLifecycle: Lifecycle.State = Lifecycle.State.INITIALIZED
    set(maxState) {
      field = maxState
      updateState()
    }

  private val _lifecycle = LifecycleRegistry()
  override val lifecycle: Lifecycle get() = _lifecycle

  internal fun handleLifecycleEvent(event: Lifecycle.Event) {
    hostLifecycleState = event.targetState
    updateState()
  }

  /**
   * Update the state to be the lower of the two constraints:
   */
  internal fun updateState() {
    println("$this updateState: $hostLifecycleState, $maxLifecycle")
    if (hostLifecycleState < maxLifecycle) {
      moveTo(hostLifecycleState)
    } else {
      moveTo(maxLifecycle)
    }
    println("$this updateState: ${_lifecycle.currentState}")
  }

  private fun moveTo(state: Lifecycle.State) {
    when (state) {
      Lifecycle.State.DESTROYED -> {
        when (_lifecycle.currentState) {
          Lifecycle.State.DESTROYED ->
            Unit

          Lifecycle.State.INITIALIZED -> {
            _lifecycle.onStateChanged(Lifecycle.Event.ON_CREATE)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_DESTROY)
          }

          Lifecycle.State.CREATED ->
            _lifecycle.onStateChanged(Lifecycle.Event.ON_DESTROY)

          Lifecycle.State.STARTED -> {
            _lifecycle.onStateChanged(Lifecycle.Event.ON_STOP)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_DESTROY)
          }

          Lifecycle.State.RESUMED -> {
            _lifecycle.onStateChanged(Lifecycle.Event.ON_PAUSE)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_STOP)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_DESTROY)
          }
        }
      }

      Lifecycle.State.INITIALIZED ->
        Unit

      Lifecycle.State.CREATED -> {
        when (_lifecycle.currentState) {
          Lifecycle.State.DESTROYED ->
            Unit

          Lifecycle.State.INITIALIZED ->
            _lifecycle.onStateChanged(Lifecycle.Event.ON_CREATE)

          Lifecycle.State.CREATED ->
            Unit

          Lifecycle.State.STARTED ->
            _lifecycle.onStateChanged(Lifecycle.Event.ON_STOP)

          Lifecycle.State.RESUMED -> {
            _lifecycle.onStateChanged(Lifecycle.Event.ON_PAUSE)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_STOP)
          }
        }
      }

      Lifecycle.State.STARTED -> {
        when (_lifecycle.currentState) {
          Lifecycle.State.INITIALIZED -> {
            _lifecycle.onStateChanged(Lifecycle.Event.ON_CREATE)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_START)
          }

          Lifecycle.State.CREATED ->
            _lifecycle.onStateChanged(Lifecycle.Event.ON_START)

          Lifecycle.State.STARTED ->
            Unit

          Lifecycle.State.RESUMED ->
            _lifecycle.onStateChanged(Lifecycle.Event.ON_PAUSE)

          Lifecycle.State.DESTROYED ->
            Unit
        }
      }

      Lifecycle.State.RESUMED -> {
        when (_lifecycle.currentState) {
          Lifecycle.State.INITIALIZED -> {
            _lifecycle.onStateChanged(Lifecycle.Event.ON_CREATE)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_START)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_RESUME)
          }

          Lifecycle.State.CREATED -> {
            _lifecycle.onStateChanged(Lifecycle.Event.ON_START)
            _lifecycle.onStateChanged(Lifecycle.Event.ON_RESUME)
          }

          Lifecycle.State.STARTED ->
            _lifecycle.onStateChanged(Lifecycle.Event.ON_RESUME)

          Lifecycle.State.RESUMED ->
            Unit

          Lifecycle.State.DESTROYED ->
            Unit
        }
      }
    }
  }
}
