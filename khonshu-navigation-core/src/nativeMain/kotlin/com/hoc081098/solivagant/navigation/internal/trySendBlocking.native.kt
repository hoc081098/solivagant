package com.hoc081098.solivagant.navigation.internal

import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking as kotlinxTrySendBlocking

internal actual fun <T> SendChannel<T>.trySendBlocking(element: T): ChannelResult<Unit> =
  kotlinxTrySendBlocking(element)
