package com.hoc081098.solivagant.sample.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hoc081098.kmp.viewmodel.koin.compose.koinKmpViewModel

@Composable
internal fun StartScreen(
  modifier: Modifier = Modifier,
  viewModel: StartViewModel = koinKmpViewModel(),
) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    ProductsButton(
      navigateToProducts = remember(viewModel) { viewModel::navigateToProductsScreen },
    )

    Spacer(modifier = Modifier.height(16.dp))

    SearchProductsButton(
      navigateToSearch = remember(viewModel) { viewModel::navigateToSearchProductScreen },
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
