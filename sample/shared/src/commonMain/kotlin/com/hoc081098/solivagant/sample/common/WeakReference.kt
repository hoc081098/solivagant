package com.hoc081098.solivagant.sample.common

internal expect class WeakReference<T : Any> constructor(reference: T) {
  fun get(): T?
}
