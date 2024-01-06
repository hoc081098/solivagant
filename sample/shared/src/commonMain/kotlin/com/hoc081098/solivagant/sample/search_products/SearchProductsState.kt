package com.hoc081098.solivagant.sample.search_products

import androidx.compose.runtime.Immutable
import com.hoc081098.solivagant.sample.common.ProductItemUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class SearchProductsState(
  val products: ImmutableList<ProductItemUi>,
  val isLoading: Boolean,
  val error: Throwable?,
  val submittedTerm: String?,
) {
  companion object {
    val INITIAL = SearchProductsState(
      products = persistentListOf(),
      isLoading = false,
      error = null,
      submittedTerm = null,
    )
  }
}
