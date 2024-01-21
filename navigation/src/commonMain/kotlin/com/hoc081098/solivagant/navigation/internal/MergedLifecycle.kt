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

package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.Lifecycle.Companion.currentState
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry

internal class MergedLifecycle private constructor(
  private val registry: LifecycleRegistry,
  lifecycle1: Lifecycle,
  lifecycle2: Lifecycle
) : Lifecycle by registry {

  constructor(lifecycle1: Lifecycle, lifecycle2: Lifecycle) : this(LifecycleRegistry(), lifecycle1, lifecycle2)

  init {
    moveTo(minOf(lifecycle1.currentState, lifecycle2.currentState))

    if (lifecycle1.currentState != Lifecycle.State.DESTROYED &&
      lifecycle2.currentState != Lifecycle.State.DESTROYED
    ) {
      val c1 = lifecycle1.subscribe { event ->
        moveTo(minOf(event.targetState, lifecycle2.currentState))
      }
      val c2 = lifecycle2.subscribe { event ->
        moveTo(minOf(event.targetState, lifecycle1.currentState))
      }

      registry.subscribe { event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
          c1.cancel()
          c2.cancel()
        }
      }
    }
  }

  private fun moveTo(state: Lifecycle.State) {
    when (state) {
      Lifecycle.State.DESTROYED -> moveToDestroyed()
      Lifecycle.State.INITIALIZED -> Unit
      Lifecycle.State.CREATED -> moveToCreated()
      Lifecycle.State.STARTED -> moveToStarted()
      Lifecycle.State.RESUMED -> moveToResumed()
    }
  }

  private fun moveToDestroyed() {
    when (registry.currentState) {
      Lifecycle.State.DESTROYED -> Unit

      Lifecycle.State.INITIALIZED -> {
        registry.onStateChanged(Lifecycle.Event.ON_CREATE)
        registry.onStateChanged(Lifecycle.Event.ON_DESTROY)
      }

      Lifecycle.State.CREATED,
      Lifecycle.State.STARTED,
      Lifecycle.State.RESUMED -> registry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }
  }

  private fun moveToCreated() {
    when (registry.currentState) {
      Lifecycle.State.DESTROYED -> Unit
      Lifecycle.State.INITIALIZED -> registry.onStateChanged(Lifecycle.Event.ON_CREATE)

      Lifecycle.State.CREATED -> Unit

      Lifecycle.State.STARTED,
      Lifecycle.State.RESUMED -> registry.onStateChanged(Lifecycle.Event.ON_STOP)
    }
  }

  private fun moveToStarted() {
    when (registry.currentState) {
      Lifecycle.State.INITIALIZED,
      Lifecycle.State.CREATED -> registry.onStateChanged(Lifecycle.Event.ON_START)

      Lifecycle.State.RESUMED -> registry.onStateChanged(Lifecycle.Event.ON_PAUSE)

      Lifecycle.State.DESTROYED,
      Lifecycle.State.STARTED -> Unit
    }
  }

  private fun moveToResumed() {
    when (registry.currentState) {
      Lifecycle.State.INITIALIZED,
      Lifecycle.State.CREATED,
      Lifecycle.State.STARTED -> registry.onStateChanged(Lifecycle.Event.ON_RESUME)

      Lifecycle.State.RESUMED,
      Lifecycle.State.DESTROYED -> Unit
    }
  }
}
