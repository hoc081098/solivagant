package com.hoc081098.solivagant.sample

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor.KtorNetworkFetcherFactory
import coil3.util.DebugLogger
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.NavHost
import com.hoc081098.solivagant.navigation.NavRoute
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
      .components { add(KtorNetworkFetcherFactory()) }
      .logger(DebugLogger())
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

  var isDarkTheme by remember { mutableStateOf(false) }

  KoinContext {
    MyApplicationTheme(
      useDarkTheme = isDarkTheme,
    ) {
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
                    StartScreenRoute -> "Solivagant Sample"
                    is ProductsScreenRoute -> "Products"
                    is SearchProductScreenRoute -> "Search products"
                    is ProductDetailScreenRoute -> "Product detail"
                    else -> "Unknown"
                  },
                )
              },
              navigationIcon = {
                if (currentRoute is NavRoute) {
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
              actions = {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it },
                  )

                  Spacer(modifier = Modifier.width(8.dp))

                  Text(
                    text = "Dark theme",
                  )

                  Spacer(modifier = Modifier.width(8.dp))
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
