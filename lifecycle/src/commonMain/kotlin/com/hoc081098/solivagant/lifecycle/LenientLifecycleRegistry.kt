package com.hoc081098.solivagant.lifecycle

import com.hoc081098.solivagant.lifecycle.Lifecycle.State
import kotlin.js.JsName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A [LifecycleRegistry] without the state checking, and has [moveTo] method to move to a specific state.
 * This is a flexible version of [LifecycleRegistry].
 */
public sealed interface LenientLifecycleRegistry : LifecycleRegistry {
  /**
   * Move to the specified [state].
   */
  public fun moveTo(state: State)
}

/**
 * Creates a default implementation of [LenientLifecycleRegistry].
 */
@JsName("lenientLifecycleRegistry")
public fun LenientLifecycleRegistry(): LenientLifecycleRegistry =
  LenientLifecycleRegistry(initialState = State.INITIALIZED)

/**
 * Creates a default implementation of [LenientLifecycleRegistry] with the specified [initialState].
 */
public fun LenientLifecycleRegistry(initialState: State): LenientLifecycleRegistry =
  LenientLifecycleRegistryImpl(initialState)

@Suppress("TooManyFunctions")
private class LenientLifecycleRegistryImpl(initialState: State) : LenientLifecycleRegistry {
  private val _currentStateFlow = MutableStateFlow(initialState)
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
      observer.onStateChanged(Lifecycle.Event.ON_CREATE)
    }
    if (state >= State.STARTED) {
      observer.onStateChanged(Lifecycle.Event.ON_START)
    }
    if (state >= State.RESUMED) {
      observer.onStateChanged(Lifecycle.Event.ON_RESUME)
    }

    return Lifecycle.Cancellable { observers -= observer }
  }

  override fun onStateChanged(event: Lifecycle.Event) = moveTo(event.targetState)

  override fun moveTo(state: State) {
    when (state) {
      State.INITIALIZED -> checkState(State.INITIALIZED)
      State.CREATED -> moveToCreated()
      State.DESTROYED -> moveToDestroyed()
      State.STARTED -> moveToStarted()
      State.RESUMED -> moveToResumed()
    }
  }

  override fun toString(): String = "LenientLifecycleRegistryImpl(currentState=$_state)"

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
    observers.forEach { it.onStateChanged(Lifecycle.Event.ON_CREATE) }
  }

  private fun onStart() {
    checkState(State.CREATED)
    _state = State.STARTED
    observers.forEach { it.onStateChanged(Lifecycle.Event.ON_START) }
  }

  private fun onResume() {
    checkState(State.STARTED)
    _state = State.RESUMED
    observers.forEach { it.onStateChanged(Lifecycle.Event.ON_RESUME) }
  }

  private fun onPause() {
    checkState(State.RESUMED)
    _state = State.STARTED
    observers.reversed().forEach { it.onStateChanged(Lifecycle.Event.ON_PAUSE) }
  }

  private fun onStop() {
    checkState(State.STARTED)
    _state = State.CREATED
    observers.asReversed().forEach { it.onStateChanged(Lifecycle.Event.ON_STOP) }
  }

  private fun onDestroy() {
    checkState(State.CREATED)
    _state = State.DESTROYED
    observers.asReversed().forEach { it.onStateChanged(Lifecycle.Event.ON_DESTROY) }
    observers = emptyList()
  }

  private fun checkState(required: State) {
    check(_state == required) { "Expected state $required but was $_state" }
  }
  //endregion
}
