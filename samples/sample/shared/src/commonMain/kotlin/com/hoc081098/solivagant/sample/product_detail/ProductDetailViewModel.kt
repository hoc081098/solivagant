@file:Suppress("PackageNaming")

package com.hoc081098.solivagant.sample.product_detail

import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.navigation.requireRoute
import com.hoc081098.solivagant.sample.common.toProductItemUi
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val getProductById: GetProductById,
) : ViewModel() {
  private val route = savedStateHandle.requireRoute<ProductDetailScreenRoute>()

  private val refreshFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  private val retryFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  private val productItemFlow = flowFromSuspend { getProductById(route.id) }
    .onStart { Napier.d("getProductById id=$route") }
    .map { ProductDetailState.Success(it.toProductItemUi()) }

  val stateFlow: StateFlow<ProductDetailState> = merge(
    // initial load & retry
    retryFlow
      .startWith(Unit)
      .flatMapLatest {
        productItemFlow
          .startWith(ProductDetailState.Loading)
          .catch { emit(ProductDetailState.Error(it)) }
      },
    // refresh flow
    refreshFlow
      .flatMapFirst {
        // doesn't show loading state and error state when refreshing
        productItemFlow.catch { }
      },
  )
    .onEach { Napier.d("stateFlow: ${it::class.simpleName}") }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Lazily,
      initialValue = ProductDetailState.Loading,
    )

  fun refresh() {
    viewModelScope.launch {
      yield()

      if (stateFlow.value is ProductDetailState.Success) {
        Napier.d("refresh...")
        refreshFlow.emit(Unit)
      }
    }
  }

  fun retry() {
    viewModelScope.launch {
      if (stateFlow.value is ProductDetailState.Error) {
        Napier.d("retry...")
        retryFlow.emit(Unit)
      }
    }
  }
}
