package com.hoc081098.solivagant.sample

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.NetworkFetcher
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.NavHost
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.sample.common.MyApplicationTheme
import com.hoc081098.solivagant.sample.product_detail.ProductDetailScreenDestination
import com.hoc081098.solivagant.sample.product_detail.ProductDetailScreenRoute
import com.hoc081098.solivagant.sample.products.ProductsScreenDestination
import com.hoc081098.solivagant.sample.products.ProductsScreenRoute
import com.hoc081098.solivagant.sample.search_products.SearchProductScreenDestination
import com.hoc081098.solivagant.sample.search_products.SearchProductScreenRoute
import com.hoc081098.solivagant.sample.start.StartScreenDestination
import com.hoc081098.solivagant.sample.start.StartScreenRoute
import kotlinx.collections.immutable.persistentSetOf
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@OptIn(
  ExperimentalLayoutApi::class,
  ExperimentalMaterial3Api::class,
  ExperimentalCoilApi::class,
)
@Composable
@Suppress("LongMethod")
fun SolivagantSampleApp(
  modifier: Modifier = Modifier,
  navigator: NavEventNavigator = koinInject(),
) {
  setSingletonImageLoaderFactory { context ->
    ImageLoader.Builder(context)
      .components { add(NetworkFetcher.Factory()) }
      .build()
  }

  var currentRoute: BaseRoute? by remember { mutableStateOf(null) }
  val destinations = remember {
    persistentSetOf(
      StartScreenDestination,
      ProductsScreenDestination,
      SearchProductScreenDestination,
      ProductDetailScreenDestination,
    )
  }

  KoinContext {
    MyApplicationTheme {
      Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
      ) {
        Scaffold(
          topBar = {
            TopAppBar(
              title = {
                Text(
                  text = when (currentRoute) {
                    StartScreenRoute -> "KMP ViewModel Sample"
                    is ProductsScreenRoute -> "Products screen"
                    is SearchProductScreenRoute -> "Search products screen"
                    is ProductDetailScreenRoute -> "Product detail screen"
                    else -> "KMP ViewModel Sample"
                  },
                )
              },
              navigationIcon = {
                if (currentRoute !is NavRoot) {
                  IconButton(
                    onClick = remember { navigator::navigateBack },
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowBack,
                      contentDescription = "Back",
                    )
                  }
                }
              },
            )
          },
        ) { innerPadding ->
          NavHost(
            modifier = Modifier
              .padding(innerPadding)
              .consumeWindowInsets(innerPadding)
              .fillMaxSize(),
            startRoute = StartScreenRoute,
            destinations = destinations,
            navEventNavigator = navigator,
            destinationChangedCallback = { currentRoute = it },
          )
        }
      }
    }
  }
}
