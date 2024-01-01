package com.freeletics.khonshu.navigation.internal

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual class WeakReference<T : Any> actual constructor(referred: T) {
  private val ref = kotlin.native.ref.WeakReference(referred)

  actual fun get(): T? = ref.get()

  actual fun clear() = ref.clear()
}
