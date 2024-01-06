package com.hoc081098.solivagant.sample

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.solivagant.sample.common.AppDispatchers
import com.hoc081098.solivagant.sample.common.DesktopAppDispatchers
import com.hoc081098.solivagant.sample.product_detail.ProductDetailViewModel
import com.hoc081098.solivagant.sample.products.ProductsViewModel
import com.hoc081098.solivagant.sample.search_products.SearchProductsViewModel
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun isDebug(): Boolean = true

internal actual val PlatformModule: Module = module {
  factoryOf(::ProductsViewModel)
  factoryOf(::SearchProductsViewModel)
  factory { params ->
    ProductDetailViewModel.create(
      id = params.get(),
      getProductById = get(),
    )
  }
  factory { SavedStateHandle() }
  singleOf(::DesktopAppDispatchers) { bind<AppDispatchers>() }
}

actual fun setupNapier() {
  if (isDebug()) {
    Napier.base(DebugAntilog())
  }
}
