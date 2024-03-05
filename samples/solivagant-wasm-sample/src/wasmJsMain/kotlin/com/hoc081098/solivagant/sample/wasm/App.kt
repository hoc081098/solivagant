package com.hoc081098.solivagant.sample.wasm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.lifecycle.compose.currentStateAsState
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.NavHost
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.sample.wasm.second.SecondScreenDestination
import com.hoc081098.solivagant.sample.wasm.start.StartScreenDestination
import com.hoc081098.solivagant.sample.wasm.start.StartScreenRoute
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

private val Navigator by lazy { NavEventNavigator() }
val LocalNavigator = staticCompositionLocalOf<NavEventNavigator> { error("No Navigator provided") }

@Stable
private val AllDestinations: ImmutableSet<NavDestination> = persistentSetOf(
  StartScreenDestination,
  SecondScreenDestination,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(modifier: Modifier = Modifier) {
  var currentRoute by rememberSaveable { mutableStateOf<BaseRoute?>(null) }
  val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()

  CompositionLocalProvider(LocalNavigator provides Navigator) {
    MaterialTheme {
      Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
      ) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            CenterAlignedTopAppBar(
              title = { Text(text = currentRoute.toString()) },
              navigationIcon = {
                if (currentRoute !is NavRoot) {
                  IconButton(onClick = remember { Navigator::navigateBack }) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Back",
                    )
                  }
                }
              },
            )
          },
        ) { innerPadding ->
          Column(
            modifier = Modifier.fillMaxSize()
              .padding(innerPadding)
              .consumeWindowInsets(innerPadding),
          ) {
            NavHost(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              startRoute = StartScreenRoute,
              destinations = AllDestinations,
              navEventNavigator = Navigator,
              destinationChangedCallback = { currentRoute = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
              text = "Lifecycle state: $lifecycleState",
              textAlign = TextAlign.Center,
              style = MaterialTheme.typography.titleMedium,
            )
          }
        }
      }
    }
  }
}
