package com.hoc081098.solivagant.sample.common

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.identityHashCode as nativeIdentityHashCode

@OptIn(ExperimentalNativeApi::class)
internal actual fun Any?.identityHashCode(): Int = nativeIdentityHashCode()
