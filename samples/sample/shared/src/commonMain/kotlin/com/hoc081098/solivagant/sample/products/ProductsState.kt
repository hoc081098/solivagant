package com.hoc081098.solivagant.sample.products

import androidx.compose.runtime.Immutable
import com.hoc081098.solivagant.sample.common.ProductItemUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ProductsState(
  val products: ImmutableList<ProductItemUi>,
  val isLoading: Boolean,
  val error: Throwable?,
  val isRefreshing: Boolean,
) {
  val hasContent: Boolean get() = products.isNotEmpty() && error == null

  companion object {
    val INITIAL = ProductsState(
      products = persistentListOf(),
      isLoading = true,
      error = null,
      isRefreshing = false,
    )
  }
}
