package com.hoc081098.solivagant.sample.products

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
import com.hoc081098.solivagant.sample.common.ProductItemUi
import com.hoc081098.solivagant.sample.common.ProductItemsList
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

@Suppress("ReturnCount", "ModifierReused", "LongMethod")
@Composable
fun ProductsScreen(
  modifier: Modifier = Modifier,
  viewModel: ProductsViewModel = koinKmpViewModel<ProductsViewModel>(),
) {
  val state by viewModel.stateFlow.collectAsStateWithLifecycle()
  val dispatch: (ProductsAction) -> Unit = remember(viewModel) { viewModel::dispatch }

  OnLifecycleEventWithBuilder(dispatch) {
    onResume {
      if (!state.hasContent) {
        dispatch(ProductsAction.Load)
      }
    }

    onEach { event -> Napier.d("[ProductsScreen] event=$event") }
  }

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

  ListContent(
    modifier = modifier,
    state = state,
    lazyListState = lazyListState,
    onRetry = { dispatch(ProductsAction.Load) },
    onRefresh = { dispatch(ProductsAction.Refresh) },
    onItemClick = { dispatch(ProductsAction.NavigateToProductDetail(it.id)) },
  )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ListContent(
  state: ProductsState,
  onItemClick: (ProductItemUi) -> Unit,
  onRetry: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  lazyListState: LazyListState = rememberLazyListState(),
) {
  Surface(modifier = modifier) {
    when {
      state.isLoading -> {
        LoadingIndicator()
      }

      state.error != null -> {
        ErrorMessageAndRetryButton(
          onRetry = onRetry,
          errorMessage = state.error.message ?: "Unknown error",
        )
      }

      state.products.isEmpty() -> {
        EmptyProducts()
      }

      else -> {
        ProductItemsList(
          products = state.products,
          pullRefreshState = rememberPullRefreshState(
            refreshing = state.isRefreshing,
            onRefresh = onRefresh,
          ),
          isRefreshing = state.isRefreshing,
          lazyListState = lazyListState,
          onItemClick = onItemClick,
        )
      }
    }
  }
}
