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

package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.Lifecycle.Event
import com.hoc081098.solivagant.lifecycle.Lifecycle.State
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("TooManyFunctions")
private class StackEntryLifecycle : Lifecycle {
  private val _currentStateFlow = MutableStateFlow(State.INITIALIZED)
  private var _state: State = _currentStateFlow.value
    set(value) {
      field = value
      _currentStateFlow.value = value
    }
  private var observers: List<Lifecycle.Observer> = emptyList()

  override val currentStateFlow: StateFlow<State>
    get() = _currentStateFlow.asStateFlow()

  override val currentState: State
    get() = _state

  override fun subscribe(observer: Lifecycle.Observer): Lifecycle.Cancellable {
    observers += observer

    val state = _state
    if (state >= State.CREATED) {
      observer.onStateChanged(Event.ON_CREATE)
    }
    if (state >= State.STARTED) {
      observer.onStateChanged(Event.ON_START)
    }
    if (state >= State.RESUMED) {
      observer.onStateChanged(Event.ON_RESUME)
    }

    return Lifecycle.Cancellable { observers -= observer }
  }

  fun moveTo(state: State) {
    when (state) {
      State.INITIALIZED -> checkState(State.INITIALIZED)
      State.CREATED -> moveToCreated()
      State.DESTROYED -> moveToDestroyed()
      State.STARTED -> moveToStarted()
      State.RESUMED -> moveToResumed()
    }
  }

  override fun toString(): String = "StackEntryLifecycle(currentState=$_state)"

  //region Move to state
  private fun moveToResumed() {
    when (currentState) {
      State.INITIALIZED -> {
        onCreate()
        onStart()
        onResume()
      }

      State.CREATED -> {
        onStart()
        onResume()
      }

      State.STARTED -> onResume()
      State.RESUMED -> Unit
      State.DESTROYED -> error("Can't move to RESUMED state from DESTROYED")
    }
  }

  private fun moveToStarted() {
    when (currentState) {
      State.INITIALIZED -> {
        onCreate()
        onStart()
      }

      State.CREATED -> onStart()
      State.STARTED -> Unit
      State.RESUMED -> onPause()
      State.DESTROYED -> error("Can't move to STARTED state from DESTROYED")
    }
  }

  private fun moveToCreated() {
    when (currentState) {
      State.DESTROYED -> onCreate()
      State.INITIALIZED -> onCreate()
      State.CREATED -> Unit
      State.STARTED -> onStop()
      State.RESUMED -> {
        onPause()
        onStop()
      }
    }
  }

  private fun moveToDestroyed() {
    when (currentState) {
      State.DESTROYED -> Unit
      State.INITIALIZED -> {
        onCreate()
        onDestroy()
      }

      State.CREATED -> onDestroy()
      State.STARTED -> {
        onStop()
        onDestroy()
      }

      State.RESUMED -> {
        onPause()
        onStop()
        onDestroy()
      }
    }
  }
  //endregion

  //region Copy from LifecycleRegistryImpl, but remove `checkState` for `onCreate`.
  private fun onCreate() {
    _state = State.CREATED
    observers.forEach { it.onStateChanged(Event.ON_CREATE) }
  }

  private fun onStart() {
    checkState(State.CREATED)
    _state = State.STARTED
    observers.forEach { it.onStateChanged(Event.ON_START) }
  }

  private fun onResume() {
    checkState(State.STARTED)
    _state = State.RESUMED
    observers.forEach { it.onStateChanged(Event.ON_RESUME) }
  }

  private fun onPause() {
    checkState(State.RESUMED)
    _state = State.STARTED
    observers.reversed().forEach { it.onStateChanged(Event.ON_PAUSE) }
  }

  private fun onStop() {
    checkState(State.STARTED)
    _state = State.CREATED
    observers.asReversed().forEach { it.onStateChanged(Event.ON_STOP) }
  }

  private fun onDestroy() {
    checkState(State.CREATED)
    _state = State.DESTROYED
    observers.asReversed().forEach { it.onStateChanged(Event.ON_DESTROY) }
    observers = emptyList()
  }

  private fun checkState(required: State) {
    check(_state == required) { "Expected state $required but was $_state" }
  }
  //endregion
}

internal class StackEntryLifecycleOwner(
  private var hostLifecycleState: State = State.CREATED,
) : LifecycleOwner {
  internal var maxLifecycle: State = State.INITIALIZED
    set(maxState) {
      field = maxState
      updateState()
    }

  private val _lifecycle = StackEntryLifecycle()
  override val lifecycle: Lifecycle get() = _lifecycle

  internal fun handleLifecycleEvent(event: Event) {
    hostLifecycleState = event.targetState
    updateState()
  }

  /**
   * Update the state to be the lower of the two constraints: [hostLifecycleState] and [maxLifecycle].
   */
  private fun updateState() {
    if (hostLifecycleState < maxLifecycle) {
      _lifecycle.moveTo(hostLifecycleState)
    } else {
      _lifecycle.moveTo(maxLifecycle)
    }
  }

  override fun toString(): String = buildString {
    append("StackEntryLifecycleOwner(")
    append("hostLifecycleState=$hostLifecycleState, ")
    append("maxLifecycle=$maxLifecycle, ")
    append("lifecycle=$lifecycle")
    append(")")
  }
}
