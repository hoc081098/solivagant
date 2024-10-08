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

package com.hoc081098.solivagant.lifecycle.internal

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Map a [StateFlow] to another [StateFlow] with the given [transform] function.
 */
@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
internal fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> =
  object : StateFlow<R> {
    override val replayCache: List<R>
      get() = this@mapState.replayCache.map(transform)

    override val value: R
      get() = transform(this@mapState.value)

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
      this@mapState
        .map { transform(it) }
        .distinctUntilChanged()
        .collect(collector)

      error("StateFlow collection never ends.")
    }
  }
