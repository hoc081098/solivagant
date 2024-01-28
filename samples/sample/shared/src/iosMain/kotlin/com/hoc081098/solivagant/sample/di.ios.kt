package com.hoc081098.solivagant.sample

import com.hoc081098.solivagant.sample.common.AppDispatchers
import com.hoc081098.solivagant.sample.common.IosAppDispatchers
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.experimental.ExperimentalNativeApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@OptIn(ExperimentalNativeApi::class)
actual fun isDebug(): Boolean = Platform.isDebugBinary

internal actual val PlatformModule: Module = module {
  singleOf(::IosAppDispatchers) { bind<AppDispatchers>() }
}

actual fun setupNapier() {
  if (isDebug()) {
    Napier.base(DebugAntilog())
  }
}
