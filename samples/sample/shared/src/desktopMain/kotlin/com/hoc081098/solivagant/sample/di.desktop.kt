package com.hoc081098.solivagant.sample

import com.hoc081098.solivagant.sample.common.AppDispatchers
import com.hoc081098.solivagant.sample.common.DesktopAppDispatchers
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun isDebug(): Boolean = true

internal actual val PlatformModule: Module = module {
  singleOf(::DesktopAppDispatchers) { bind<AppDispatchers>() }
}

actual fun setupNapier() {
  if (isDebug()) {
    Napier.base(DebugAntilog())
  }
}
