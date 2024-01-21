package com.hoc081098.solivagant.lifecycle.internal

internal actual class AtomicReference<T> actual constructor(value: T) {
  private val delegate = kotlin.concurrent.AtomicReference(value)

  actual fun set(value: T) {
    delegate.value = value
  }

  actual fun getAndSet(value: T): T = delegate.getAndSet(value)
}
