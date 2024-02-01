package com.hoc081098.solivagant.sample.simple

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

actual fun isDebug(): Boolean = true

actual fun setupNapier() {
  if (isDebug()) {
    Napier.base(DebugAntilog())
  }
}
