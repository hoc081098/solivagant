package com.hoc081098.solivagant.lifecycle.internal

internal actual class AtomicReference<T> actual constructor(value: T) {
  private var _value: T = value

  actual fun set(value: T) {
    _value = value
  }

  actual fun getAndSet(value: T): T = _value.also { _value = value }
}
