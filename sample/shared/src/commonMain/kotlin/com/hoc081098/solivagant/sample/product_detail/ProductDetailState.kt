package com.hoc081098.solivagant.sample.product_detail

import androidx.compose.runtime.Immutable
import com.hoc081098.solivagant.sample.common.ProductItemUi

@Immutable
sealed interface ProductDetailState {
  data class Success(val product: ProductItemUi) : ProductDetailState
  data object Loading : ProductDetailState
  data class Error(val error: Throwable) : ProductDetailState
}
