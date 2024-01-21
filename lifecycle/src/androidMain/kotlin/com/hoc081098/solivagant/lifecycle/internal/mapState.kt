package com.hoc081098.solivagant.lifecycle.internal

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

/**
 * Map a [StateFlow] to another [StateFlow] with the given [transform] function.
 */
internal fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> =
  object : StateFlow<R> {
    override val replayCache: List<R>
      get() = listOf(value)

    override val value: R
      get() = transform(this@mapState.value)

    override suspend fun collect(collector: FlowCollector<R>): Nothing =
      this@mapState.collect { collector.emit(transform(it)) }
  }
