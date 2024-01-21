package com.hoc081098.solivagant.lifecycle.internal

import java.util.concurrent.atomic.AtomicReference

// TODO: https://youtrack.jetbrains.com/issue/KT-37316
@Suppress("ACTUAL_WITHOUT_EXPECT") // internal expect is not matched with internal typealias to public type
internal actual typealias AtomicReference<T> = AtomicReference<T>
