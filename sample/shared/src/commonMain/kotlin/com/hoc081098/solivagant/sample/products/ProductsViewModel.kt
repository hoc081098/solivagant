package com.hoc081098.solivagant.sample.products

import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.ignoreElements
import com.hoc081098.flowext.startWith
import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.wrapper.NonNullFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.NonNullStateFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.wrap
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.sample.common.SingleEventChannel
import com.hoc081098.solivagant.sample.common.toProductItemUi
import com.hoc081098.solivagant.sample.product_detail.ProductDetailScreenRoute
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

sealed interface ProductSingleEvent {
  sealed interface Refresh : ProductSingleEvent {
    data object Success : Refresh
    data class Failure(val error: Throwable) : Refresh
  }
}

sealed interface ProductsAction {
  data object Load : ProductsAction
  data object Refresh : ProductsAction
  data class NavigateToProductDetail(val id: Int) : ProductsAction
}

private fun interface Reducer {
  operator fun invoke(state: ProductsState): ProductsState
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModel(
  private val getProducts: GetProducts,
  private val singleEventChannel: SingleEventChannel<ProductSingleEvent>,
  private val navigator: NavEventNavigator,
) : ViewModel(
  Closeable { Napier.d("[DEMO] Closable 1 ...") },
  Closeable { Napier.d("[DEMO] Closable 2 ...") },
  singleEventChannel,
) {
  private val _actionFlow = MutableSharedFlow<ProductsAction>(Int.MAX_VALUE)

  val stateFlow: NonNullStateFlowWrapper<ProductsState>
  val eventFlow: NonNullFlowWrapper<ProductSingleEvent> = singleEventChannel.singleEventFlow.wrap()

  init {
    addCloseable { Napier.d("[DEMO] Closable 3 ...") }

    stateFlow = merge(
      _actionFlow
        .filterIsInstance<ProductsAction.Load>()
        .loadFlow(),
      _actionFlow
        .filterIsInstance<ProductsAction.Refresh>()
        .refreshFlow(),
      _actionFlow
        .filterIsInstance<ProductsAction.NavigateToProductDetail>()
        .navigateToProductDetailFlow(),
    )
      .scan(ProductsState.INITIAL) { state, reducer -> reducer(state) }
      .onEach {
        Napier.d(
          "State: products=${it.products.size}, isLoading=${it.isLoading}," +
              " error=${it.error}, isRefreshing=${it.isRefreshing}",
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ProductsState.INITIAL,
      )
      .wrap()
  }

  //region Handlers
  private fun Flow<ProductsAction.Refresh>.refreshFlow(): Flow<Reducer> =
    flatMapFirst {
      flowFromSuspend { getProducts() }
        .map { products ->
          singleEventChannel.sendEvent(ProductSingleEvent.Refresh.Success)

          Reducer { state ->
            state.copy(
              products = products
                .map { it.toProductItemUi() }
                .toImmutableList(),
              isRefreshing = false,
              error = null,
            )
          }
        }
        .catch { throwable ->
          singleEventChannel.sendEvent(ProductSingleEvent.Refresh.Failure(throwable))
          emit(Reducer { it.copy(isRefreshing = false) })
        }
        .startWith { Reducer { it.copy(isRefreshing = true) } }
    }

  private fun Flow<ProductsAction.Load>.loadFlow(): Flow<Reducer> =
    flatMapLatest {
      flowFromSuspend { getProducts() }
        .map { products ->
          Reducer { state ->
            state.copy(
              products = products
                .map { it.toProductItemUi() }
                .toImmutableList(),
              isLoading = false,
              error = null,
            )
          }
        }
        .catch { error ->
          emit(
            Reducer {
              it.copy(
                isLoading = false,
                error = error,
              )
            },
          )
        }
        .startWith {
          Reducer {
            it.copy(
              isLoading = true,
              error = null,
            )
          }
        }
    }

  private fun Flow<ProductsAction.NavigateToProductDetail>.navigateToProductDetailFlow(): Flow<Nothing> =
    onEach { navigator.navigateTo(ProductDetailScreenRoute(id = it.id)) }
      .ignoreElements()
  //endregion

  fun dispatch(action: ProductsAction) {
    _actionFlow.tryEmit(action)
  }
}
