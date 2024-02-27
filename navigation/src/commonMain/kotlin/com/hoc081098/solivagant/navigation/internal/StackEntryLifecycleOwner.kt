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

import com.hoc081098.solivagant.lifecycle.LenientLifecycleRegistry
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.Lifecycle.Event
import com.hoc081098.solivagant.lifecycle.Lifecycle.State
import com.hoc081098.solivagant.lifecycle.LifecycleOwner

internal class StackEntryLifecycleOwner(
  private var hostLifecycleState: State = State.CREATED,
) : LifecycleOwner {
  internal var maxLifecycle: State = State.INITIALIZED
    set(maxState) {
      field = maxState
      updateState()
    }

  private val _lifecycle = LenientLifecycleRegistry()
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

  override fun toString(): String =
    buildString {
      append("StackEntryLifecycleOwner(")
      append("hostLifecycleState=$hostLifecycleState, ")
      append("maxLifecycle=$maxLifecycle, ")
      append("lifecycle=$lifecycle")
      append(")")
    }
}
