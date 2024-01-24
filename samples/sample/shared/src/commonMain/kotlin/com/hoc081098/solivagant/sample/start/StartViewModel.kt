package com.hoc081098.solivagant.sample.start

import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.sample.products.ProductsScreenRoute
import com.hoc081098.solivagant.sample.search_products.SearchProductScreenRoute

class StartViewModel(
  private val navigator: NavEventNavigator,
) : ViewModel() {
  internal fun navigateToProductsScreen() = navigator.navigateTo(ProductsScreenRoute)

  internal fun navigateToSearchProductScreen() = navigator.navigateTo(SearchProductScreenRoute)
}
