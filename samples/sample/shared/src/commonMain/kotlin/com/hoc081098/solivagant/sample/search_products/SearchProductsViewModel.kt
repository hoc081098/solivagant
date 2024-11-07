@file:Suppress("PackageNaming")

package com.hoc081098.solivagant.sample.search_products

import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.safe.NullableSavedStateHandleKey
import com.hoc081098.kmp.viewmodel.safe.safe
import com.hoc081098.kmp.viewmodel.safe.string
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.sample.presentation.toProductItemUi
import com.hoc081098.solivagant.sample.product_detail.ProductDetailScreenRoute
import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchProductsViewModel(
  private val searchProducts: SearchProducts,
  private val savedStateHandle: SavedStateHandle,
  private val navigator: NavEventNavigator,
) : ViewModel() {
  val searchTermStateFlow: StateFlow<String?> =
    savedStateHandle.safe.getStateFlow(SEARCH_TERM_KEY)

  val stateFlow: StateFlow<SearchProductsState> = searchTermStateFlow
    .debounce(400.milliseconds)
    .map { it.orEmpty().trim() }
    .distinctUntilChanged()
    .flatMapLatest(searchProducts::executeSearching)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Lazily,
      initialValue = SearchProductsState.INITIAL,
    )

  fun search(term: String) = savedStateHandle.safe { it[SEARCH_TERM_KEY] = term }

  fun navigateToProductDetail(id: Int) = navigator.navigateTo(ProductDetailScreenRoute(id))

  companion object {
    private val SEARCH_TERM_KEY = NullableSavedStateHandleKey.string("com.hoc081098.kmpviewmodelsample.search_term")
  }
}

private fun SearchProducts.executeSearching(term: String): Flow<SearchProductsState> {
  return flowFromSuspend { if (term.isEmpty()) emptyList() else invoke(term) }
    .onStart { Napier.d("search products term=$term") }
    .map { products ->
      SearchProductsState(
        products = products
          .map { it.toProductItemUi() }
          .toImmutableList(),
        isLoading = false,
        error = null,
        submittedTerm = term,
      )
    }
    .startWith {
      SearchProductsState(
        isLoading = true,
        error = null,
        submittedTerm = term,
        products = persistentListOf(),
      )
    }
    .catch { error ->
      Napier.e("search products failed", error)
      emit(
        SearchProductsState(
          isLoading = false,
          error = error,
          submittedTerm = term,
          products = persistentListOf(),
        ),
      )
    }
}
