@file:Suppress("PackageNaming")

package com.hoc081098.solivagant.sample.product_detail

import com.hoc081098.solivagant.sample.common.AppDispatchers
import com.hoc081098.solivagant.sample.data.FakeProductsJson
import com.hoc081098.solivagant.sample.data.ProductResponse
import com.hoc081098.solivagant.sample.data.toProductItem
import com.hoc081098.solivagant.sample.domain.ProductItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GetProductById(
  private val appDispatchers: AppDispatchers,
) {
  suspend operator fun invoke(id: Int): ProductItem =
    withContext(appDispatchers.io) {
      @Suppress("MagicNumber")
      delay(2_000)

      Json
        .decodeFromString<List<ProductResponse>>(FakeProductsJson)
        .find { it.id == id }
        ?.toProductItem()
        ?:
        @Suppress("TooGenericExceptionThrown")
        throw RuntimeException("Product with id = $id not found")
    }
}
