package com.hoc081098.solivagant.navigation.test

import com.hoc081098.kmp.viewmodel.Closeable
import kotlin.concurrent.Volatile

internal class FakeCloseable : Closeable {
  @Volatile
  var closed = false

  override fun close() {
    closed = true
  }
}
