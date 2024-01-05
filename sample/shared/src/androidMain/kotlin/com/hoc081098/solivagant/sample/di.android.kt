package com.hoc081098.solivagant.sample

import com.hoc081098.solivagant.sample.common.AndroidAppDispatchers
import com.hoc081098.solivagant.sample.common.AppDispatchers
import com.hoc081098.solivagant.sample.product_detail.ProductDetailViewModel
import com.hoc081098.solivagant.sample.products.ProductsViewModel
import com.hoc081098.solivagant.sample.search_products.SearchProductsViewModel
import io.github.aakira.napier.BuildConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun isDebug(): Boolean = BuildConfig.DEBUG

internal actual val PlatformModule: Module = module {
  viewModelOf(::ProductsViewModel)
  viewModelOf(::SearchProductsViewModel)
  viewModelOf(::ProductDetailViewModel)
  singleOf(::AndroidAppDispatchers) { bind<AppDispatchers>() }
}

actual fun setupNapier() {
  if (BuildConfig.DEBUG) {
    Napier.base(DebugAntilog())
  }
}
