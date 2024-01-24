package com.hoc081098.solivagant.sample.products

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import com.hoc081098.kmp.viewmodel.koin.compose.koinKmpViewModel
import com.hoc081098.solivagant.lifecycle.compose.collectAsStateWithLifecycle
import com.hoc081098.solivagant.sample.common.CollectWithLifecycleEffect
import com.hoc081098.solivagant.sample.common.EmptyProducts
import com.hoc081098.solivagant.sample.common.ErrorMessageAndRetryButton
import com.hoc081098.solivagant.sample.common.LoadingIndicator
import com.hoc081098.solivagant.sample.common.OnLifecycleEventWithBuilder
import com.hoc081098.solivagant.sample.common.PlatformToastManager
import com.hoc081098.solivagant.sample.common.ProductItemsList
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Suppress("ReturnCount", "ModifierReused", "LongMethod")
@Composable
fun ProductsScreen(
  modifier: Modifier = Modifier,
  viewModel: ProductsViewModel = koinKmpViewModel<ProductsViewModel>(),
) {
  DisposableEffect(viewModel) {
    if (!viewModel.stateFlow.value.hasContent) {
      viewModel.dispatch(ProductsAction.Load)
    }
    onDispose { }
  }

  OnLifecycleEventWithBuilder {
    onEach { event -> Napier.d("[ProductsScreen] event=$event") }
  }

  val state by viewModel.stateFlow.collectAsStateWithLifecycle()

  val scope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()
  val currentLazyListState by rememberUpdatedState(lazyListState)

  val toastManager = PlatformToastManager()
  val eventHandler: (ProductSingleEvent) -> Unit = remember(toastManager, scope, currentLazyListState) {
    {
        event ->
      when (event) {
        is ProductSingleEvent.Refresh.Failure -> {
          toastManager.showToast("Failed to refresh")
        }

        ProductSingleEvent.Refresh.Success -> {
          scope.launch {
            withFrameMillis { }
            currentLazyListState.animateScrollToItem(index = 0)
          }
        }
      }
    }
  }
  viewModel.eventFlow.CollectWithLifecycleEffect(collector = eventHandler)

  if (state.isLoading) {
    LoadingIndicator(modifier = modifier)
    return
  }

  state.error?.let { error ->
    ErrorMessageAndRetryButton(
      modifier = modifier,
      onRetry = { viewModel.dispatch(ProductsAction.Load) },
      errorMessage = error.message ?: "Unknown error",
    )
    return
  }

  val products = state.products.ifEmpty {
    EmptyProducts(modifier = modifier)
    return
  }
  ProductItemsList(
    modifier = modifier,
    products = products,
    pullRefreshState = rememberPullRefreshState(
      refreshing = state.isRefreshing,
      onRefresh = { viewModel.dispatch(ProductsAction.Refresh) },
    ),
    isRefreshing = state.isRefreshing,
    lazyListState = lazyListState,
    onItemClick = { viewModel.dispatch(ProductsAction.NavigateToProductDetail(it.id)) },
  )
}
