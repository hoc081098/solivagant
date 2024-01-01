package com.hoc081098.solivagant.navigation.internal

internal expect class WeakReference<T : Any> constructor(referred: T) {
  fun get(): T?

  fun clear()
}
