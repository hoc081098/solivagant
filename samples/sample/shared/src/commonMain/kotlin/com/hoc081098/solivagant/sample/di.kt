package com.hoc081098.solivagant.sample

import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.sample.common.SingleEventChannel
import com.hoc081098.solivagant.sample.product_detail.GetProductById
import com.hoc081098.solivagant.sample.products.GetProducts
import com.hoc081098.solivagant.sample.search_products.SearchProducts
import com.hoc081098.solivagant.sample.start.StartViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private val CommonModule = module {
  factoryOf(::GetProducts)
  factoryOf(::SearchProducts)
  factoryOf(::GetProductById)
  factoryOf(::StartViewModel)

  factory { SingleEventChannel<Any?>() }
  singleOf(::NavEventNavigator)
}

internal expect val PlatformModule: Module

expect fun setupNapier()

expect fun isDebug(): Boolean

fun startKoinCommon(
  appDeclaration: KoinAppDeclaration = {},
) {
  startKoin {
    appDeclaration()
    modules(CommonModule, PlatformModule)
  }
}
