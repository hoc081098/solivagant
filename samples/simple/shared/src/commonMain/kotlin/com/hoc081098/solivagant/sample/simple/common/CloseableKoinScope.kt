package com.hoc081098.solivagant.sample.simple.common

import com.hoc081098.kmp.viewmodel.Closeable
import kotlin.jvm.JvmField
import org.koin.core.scope.Scope

class CloseableKoinScope(
  @JvmField val scope: Scope,
) : Closeable {
  override fun close() = scope.close()
}
