package com.hoc081098.solivagant.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.ScreenDestination
import com.hoc081098.solivagant.sample.products.ProductsScreenRoute
import com.hoc081098.solivagant.sample.search_products.SearchProductScreenRoute
import kotlin.jvm.JvmField
import org.koin.compose.koinInject

@Parcelize
data object StartScreenRoute : NavRoute, NavRoot

@JvmField
val StartScreenDestination: NavDestination =
  ScreenDestination<StartScreenRoute> { StartScreen() }

@Composable
internal fun StartScreen(
  modifier: Modifier = Modifier,
  navigator: NavEventNavigator = koinInject(),
) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    ProductsButton(
      navigateToProducts = { navigator.navigateTo(ProductsScreenRoute) },
    )

    Spacer(modifier = Modifier.height(16.dp))

    SearchProductsButton(
      navigateToSearch = { navigator.navigateTo(SearchProductScreenRoute) },
    )
  }
}

@Composable
private fun SearchProductsButton(
  navigateToSearch: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Button(
    modifier = modifier,
    onClick = navigateToSearch,
  ) {
    Text(
      text = "Search products screen",
    )
  }
}

@Composable
private fun ProductsButton(
  navigateToProducts: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Button(
    modifier = modifier,
    onClick = navigateToProducts,
  ) {
    Text(
      text = "Products screen",
    )
  }
}
