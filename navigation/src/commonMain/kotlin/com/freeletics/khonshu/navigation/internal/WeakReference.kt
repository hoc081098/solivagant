package com.freeletics.khonshu.navigation.internal

internal expect class WeakReference<T : Any> constructor(referred: T) {
  fun get(): T?

  fun clear()
}
