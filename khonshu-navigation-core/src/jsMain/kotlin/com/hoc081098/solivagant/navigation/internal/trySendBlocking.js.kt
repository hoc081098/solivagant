package com.hoc081098.solivagant.navigation.internal

import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel

internal actual fun <T> SendChannel<T>.trySendBlocking(element: T): ChannelResult<Unit> =
  trySend(element)
