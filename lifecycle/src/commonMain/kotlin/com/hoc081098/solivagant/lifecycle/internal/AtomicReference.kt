package com.hoc081098.solivagant.lifecycle.internal

internal expect class AtomicReference<T>(value: T) {
  fun set(value: T)
  fun getAndSet(value: T): T
}
