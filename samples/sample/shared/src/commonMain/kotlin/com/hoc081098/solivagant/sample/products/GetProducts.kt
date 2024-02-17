package com.hoc081098.solivagant.sample.products

import com.hoc081098.solivagant.sample.common.AppDispatchers
import com.hoc081098.solivagant.sample.data.FakeProductsJson
import com.hoc081098.solivagant.sample.data.ProductItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GetProducts(
  private val appDispatchers: AppDispatchers,
) {
  private val mutex = Mutex()
  private var i = 0

  suspend operator fun invoke(): List<ProductItem> =
    mutex.withLock {
      withContext(appDispatchers.io) {
        @Suppress("MagicNumber")
        delay(2_000)

        if (i++ % 2 == 0) {
          @Suppress("TooGenericExceptionThrown")
          throw RuntimeException("Fake error")
        }

        Json.decodeFromString<List<ProductItem>>(FakeProductsJson).shuffled()
      }
    }
}
