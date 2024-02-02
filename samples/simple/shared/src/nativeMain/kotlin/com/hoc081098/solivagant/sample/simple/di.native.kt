package com.hoc081098.solivagant.sample.simple

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual fun isDebug(): Boolean = Platform.isDebugBinary

actual fun setupNapier() {
  if (isDebug()) {
    Napier.base(DebugAntilog())
  }
}
