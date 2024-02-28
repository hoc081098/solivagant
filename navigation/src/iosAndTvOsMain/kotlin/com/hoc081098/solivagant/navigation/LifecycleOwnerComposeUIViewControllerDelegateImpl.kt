package com.hoc081098.solivagant.navigation

import com.hoc081098.solivagant.lifecycle.LenientLifecycleRegistry
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner

internal class LifecycleOwnerComposeUIViewControllerDelegateImpl(
  hostLifecycleOwner: LifecycleOwner?,
) : LifecycleOwnerComposeUIViewControllerDelegate {
  private var maxLifecycle: Lifecycle.State = Lifecycle.State.INITIALIZED
    set(value) {
      field = value
      updateState()
    }
  private var hostLifecycleState: Lifecycle.State = Lifecycle.State.CREATED
    set(value) {
      field = value
      updateState()
    }

  private val lifecycleRegistry = LenientLifecycleRegistry()

  /**
   * Update the state to be the lower of the two constraints: [hostLifecycleState] and [maxLifecycle].
   */
  private fun updateState() = lifecycleRegistry.moveTo(minOf(hostLifecycleState, maxLifecycle))

  private val cancellable =
    hostLifecycleOwner
      ?.lifecycle
      ?.subscribe { hostLifecycleState = it.targetState }

  override val lifecycle: Lifecycle get() = lifecycleRegistry

  override fun onDestroy() {
    maxLifecycle = Lifecycle.State.DESTROYED
    cancellable?.cancel()
  }

  override fun viewDidLoad() {
    maxLifecycle = Lifecycle.Event.ON_CREATE.targetState
  }

  override fun viewWillAppear(animated: Boolean) {
    maxLifecycle = Lifecycle.Event.ON_START.targetState
  }

  override fun viewDidAppear(animated: Boolean) {
    maxLifecycle = Lifecycle.Event.ON_RESUME.targetState
  }

  override fun viewWillDisappear(animated: Boolean) {
    maxLifecycle = Lifecycle.Event.ON_PAUSE.targetState
  }

  override fun viewDidDisappear(animated: Boolean) {
    maxLifecycle = Lifecycle.Event.ON_STOP.targetState
  }

  override fun toString(): String =
    super.toString() +
      buildString {
        append("(")
        append("(hostLifecycleState=$hostLifecycleState, ")
        append("maxLifecycle=$maxLifecycle, ")
        append("lifecycle=$lifecycle")
        append(")")
      }
}
