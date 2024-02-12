package com.hoc081098.solivagant.sample.simple.ui.detail

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val DetailModule = module {
  scope<DetailScreenRoute> {
    scoped { DetailNavigator() }
    factoryOf(::DetailViewModel)
  }
}
