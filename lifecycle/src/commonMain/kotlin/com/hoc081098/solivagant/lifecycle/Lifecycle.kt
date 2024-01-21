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

/*
 * Copyright (C) 2017 The Android Open Source Project
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

import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.StateFlow

/**
 * A holder of [Lifecycle.State] that can be observed for changes.
 *
 * Possible transitions:
 *
 * ```
 * [INITIALIZED] ──┐
 *                 ↓
 *         ┌── [CREATED] ──┐
 *         ↓       ↑       ↓
 *    [DESTROYED]  └── [STARTED] ──┐
 *                         ↑       ↓
 *                         └── [RESUMED]
 * ```
 */
public interface Lifecycle {
  public val currentStateFlow: StateFlow<State>

  public fun subscribe(observer: Observer): Cancellable

  public fun interface Observer {
    public fun onStateChanged(event: Event)
  }

  public fun interface Cancellable {
    public fun cancel()
  }

  /**
   * Defines the possible states of the [Lifecycle].
   */
  public enum class State {
    DESTROYED,
    INITIALIZED,
    CREATED,
    STARTED,
    RESUMED,
    ;

    /**
     * Compares if this State is greater or equal to the given `state`.
     *
     * @param state State to compare with
     * @return true if this State is greater or equal to the given `state`
     */
    public fun isAtLeast(state: State): Boolean = compareTo(state) >= 0
  }

  public enum class Event {
    ON_CREATE,
    ON_START,
    ON_RESUME,
    ON_PAUSE,
    ON_STOP,
    ON_DESTROY,
    ;

    /**
     * Returns the new [Lifecycle.State] of a [Lifecycle] that just reported
     * this [Lifecycle.Event].
     *
     * @return the state that will result from this event
     */
    public val targetState: State
      get() {
        return when (this) {
          ON_CREATE, ON_STOP -> State.CREATED
          ON_START, ON_PAUSE -> State.STARTED
          ON_RESUME -> State.RESUMED
          ON_DESTROY -> State.DESTROYED
        }
      }

    public companion object {
      /**
       * Returns the [Lifecycle.Event] that will be reported by a [Lifecycle]
       * leaving the specified [Lifecycle.State] to a lower state, or `null`
       * if there is no valid event that can move down from the given state.
       *
       * @param state the higher state that the returned event will transition down from
       * @return the event moving down the lifecycle phases from state
       */
      @JvmStatic
      public fun downFrom(state: State): Event? {
        return when (state) {
          State.CREATED -> ON_DESTROY
          State.STARTED -> ON_STOP
          State.RESUMED -> ON_PAUSE
          else -> null
        }
      }

      /**
       * Returns the [Lifecycle.Event] that will be reported by a [Lifecycle]
       * entering the specified [Lifecycle.State] from a higher state, or `null`
       * if there is no valid event that can move down to the given state.
       *
       * @param state the lower state that the returned event will transition down to
       * @return the event moving down the lifecycle phases to state
       */
      @JvmStatic
      public fun downTo(state: State): Event? {
        return when (state) {
          State.DESTROYED -> ON_DESTROY
          State.CREATED -> ON_STOP
          State.STARTED -> ON_PAUSE
          else -> null
        }
      }

      /**
       * Returns the [Lifecycle.Event] that will be reported by a [Lifecycle]
       * leaving the specified [Lifecycle.State] to a higher state, or `null`
       * if there is no valid event that can move up from the given state.
       *
       * @param state the lower state that the returned event will transition up from
       * @return the event moving up the lifecycle phases from state
       */
      @JvmStatic
      public fun upFrom(state: State): Event? {
        return when (state) {
          State.INITIALIZED -> ON_CREATE
          State.CREATED -> ON_START
          State.STARTED -> ON_RESUME
          else -> null
        }
      }

      /**
       * Returns the [Lifecycle.Event] that will be reported by a [Lifecycle]
       * entering the specified [Lifecycle.State] from a lower state, or `null`
       * if there is no valid event that can move up to the given state.
       *
       * @param state the higher state that the returned event will transition up to
       * @return the event moving up the lifecycle phases to state
       */
      @JvmStatic
      public fun upTo(state: State): Event? {
        return when (state) {
          State.CREATED -> ON_CREATE
          State.STARTED -> ON_START
          State.RESUMED -> ON_RESUME
          else -> null
        }
      }
    }
  }

  public companion object {
    public val Lifecycle.currentState: State
      get() = currentStateFlow.value
  }
}
