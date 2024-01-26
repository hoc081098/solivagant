package com.hoc081098.solivagant.sample

import com.hoc081098.solivagant.sample.common.AndroidAppDispatchers
import com.hoc081098.solivagant.sample.common.AppDispatchers
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun isDebug(): Boolean = BuildConfig.DEBUG

internal actual val PlatformModule: Module = module {
  singleOf(::AndroidAppDispatchers) { bind<AppDispatchers>() }
}

actual fun setupNapier() {
  if (BuildConfig.DEBUG) {
    Napier.base(DebugAntilog())
  }
}
