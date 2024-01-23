/*
 * Copyright 2024 Arkadii Ivanov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hoc081098.solivagant.lifecycle

import com.hoc081098.solivagant.lifecycle.Lifecycle.Cancellable
import com.hoc081098.solivagant.lifecycle.Lifecycle.Event
import com.hoc081098.solivagant.lifecycle.Lifecycle.Observer
import com.hoc081098.solivagant.lifecycle.Lifecycle.State
import kotlin.js.JsName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents [Lifecycle] and [Lifecycle.Observer] at the same time.
 * Can be used to manually control the [Lifecycle].
 */
public interface LifecycleRegistry : Lifecycle, Observer

/**
 * Creates a default implementation of [LifecycleRegistry].
 */
@JsName("lifecycleRegistry")
public fun LifecycleRegistry(): LifecycleRegistry = LifecycleRegistry(initialState = State.INITIALIZED)

/**
 * Creates a default implementation of [LifecycleRegistry] with the specified [initialState].
 */
public fun LifecycleRegistry(
  initialState: State,
): LifecycleRegistry = LifecycleRegistryImpl(initialState)

/**
 * Possible transitions:
 *
 * ```
 * [INITIALIZED] ──┐(1)
 *                 ↓
 *      (6)┌── [CREATED] ────┐(2)
 *         ↓       ↑ (5)     ↓
 *    [DESTROYED]  └──── [STARTED] ──┐(3)
 *                           ↑       ↓
 *                        (4)└── [RESUMED]
 *
 * (1): ON_CREATE
 * (2): ON_START
 * (3): ON_RESUME
 * (4): ON_PAUSE
 * (5): ON_STOP
 * (6): ON_DESTROY
 * ```
 */
private class LifecycleRegistryImpl(initialState: State) : LifecycleRegistry {
  private val _currentStateFlow = MutableStateFlow(initialState)

  private var _state: State = _currentStateFlow.value
    set(value) {
      field = value
      _currentStateFlow.value = value
    }

  private var observers: List<Observer> = emptyList()

  override val currentStateFlow: StateFlow<State>
    get() = _currentStateFlow.asStateFlow()

  override val currentState: State
    get() = _state

  override fun onStateChanged(event: Event) {
    when (event) {
      Event.ON_CREATE -> onCreate()
      Event.ON_START -> onStart()
      Event.ON_RESUME -> onResume()
      Event.ON_PAUSE -> onPause()
      Event.ON_STOP -> onStop()
      Event.ON_DESTROY -> onDestroy()
    }
  }

  override fun subscribe(observer: Observer): Cancellable {
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

    return Cancellable { observers -= observer }
  }

  private fun onCreate() {
    checkState(State.INITIALIZED)
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
}
