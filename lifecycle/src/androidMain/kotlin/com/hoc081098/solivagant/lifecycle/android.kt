package com.hoc081098.solivagant.lifecycle

import androidx.lifecycle.Lifecycle as AndroidXLifecycle
import androidx.lifecycle.Lifecycle.Event as AndroidXLifecycleEvent
import androidx.lifecycle.Lifecycle.State as AndroidXLifecycleState
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner as AndroidXLifecycleOwner
import com.hoc081098.solivagant.lifecycle.Lifecycle.Cancellable
import com.hoc081098.solivagant.lifecycle.Lifecycle.Event
import com.hoc081098.solivagant.lifecycle.Lifecycle.State
import com.hoc081098.solivagant.lifecycle.internal.mapState
import kotlin.LazyThreadSafetyMode.NONE
import kotlinx.coroutines.flow.StateFlow

public fun AndroidXLifecycle.asSolivagantLifecycle(): Lifecycle =
  SolivagantLifecycleInterop(this)

public fun AndroidXLifecycleOwner.asSolivagantLifecycleOwner(): LifecycleOwner =
  SolivagantLifecycleOwnerInterop(this)

public fun AndroidXLifecycleState.asSolivagantState(): State = when (this) {
  AndroidXLifecycleState.DESTROYED -> State.DESTROYED
  AndroidXLifecycleState.INITIALIZED -> State.INITIALIZED
  AndroidXLifecycleState.CREATED -> State.CREATED
  AndroidXLifecycleState.STARTED -> State.STARTED
  AndroidXLifecycleState.RESUMED -> State.RESUMED
}

public fun AndroidXLifecycleEvent.asSolivagantEventOrNull(): Event? = when (this) {
  AndroidXLifecycleEvent.ON_CREATE -> Event.ON_CREATE
  AndroidXLifecycleEvent.ON_START -> Event.ON_START
  AndroidXLifecycleEvent.ON_RESUME -> Event.ON_RESUME
  AndroidXLifecycleEvent.ON_PAUSE -> Event.ON_PAUSE
  AndroidXLifecycleEvent.ON_STOP -> Event.ON_STOP
  AndroidXLifecycleEvent.ON_DESTROY -> Event.ON_DESTROY
  AndroidXLifecycleEvent.ON_ANY -> null
}

private class SolivagantLifecycleInterop(
  private val delegate: AndroidXLifecycle,
) : Lifecycle {
  override val currentStateFlow: StateFlow<State> by lazy(NONE) {
    delegate.currentStateFlow.mapState { it.asSolivagantState() }
  }

  override fun subscribe(observer: Lifecycle.Observer): Cancellable {
    // @MainThread
    var removedObserver = false

    val lifecycleEventObserver = object : LifecycleEventObserver {
      override fun onStateChanged(source: AndroidXLifecycleOwner, event: AndroidXLifecycleEvent) {
        event
          .asSolivagantEventOrNull()
          ?.let(observer::onStateChanged)

        // remove observer when ON_DESTROY
        if (event == AndroidXLifecycleEvent.ON_DESTROY) {
          delegate.removeObserver(this)
          removedObserver = true
        }
      }
    }.also(delegate::addObserver)

    return Cancellable {
      if (!removedObserver) {
        delegate.removeObserver(lifecycleEventObserver)
      }
    }
  }
}

private class SolivagantLifecycleOwnerInterop(
  private val delegate: AndroidXLifecycleOwner,
) : LifecycleOwner {
  override val lifecycle: Lifecycle by lazy(NONE) {
    delegate.lifecycle.asSolivagantLifecycle()
  }
}
