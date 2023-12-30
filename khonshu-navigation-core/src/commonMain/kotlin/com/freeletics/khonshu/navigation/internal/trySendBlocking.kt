package com.freeletics.khonshu.navigation.internal

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult

internal fun <T> Channel<T>.trySendBlocking(element: T): ChannelResult<Unit> {
  TODO()
  return trySend(element)
}
