package com.hoc081098.solivagant.sample.wasm

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.NavHost
import com.hoc081098.solivagant.navigation.NavHostDefaults
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.ScreenDestination
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import solivagant.samples.`solivagant-wasm-sample`.generated.resources.Res
import solivagant.samples.`solivagant-wasm-sample`.generated.resources.compose_multiplatform

val navigator by lazy { NavEventNavigator() }

data object StartRoute : NavRoot

val StartDest = ScreenDestination<StartRoute> { route, modifier ->
  Text("Start ok")
}

@Stable
private val AllDestinations: ImmutableSet<NavDestination> = persistentSetOf(
  StartDest,
)

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App(
  modifier: Modifier = Modifier,
) {
  var currentRoute by rememberSaveable { mutableStateOf<BaseRoute?>(null) }

  Image(painterResource(Res.drawable.compose_multiplatform), null)

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
                IconButton(onClick = remember { navigator::navigateBack }) {
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
        NavHost(
          modifier = Modifier.fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding),
          startRoute = StartRoute,
          destinations = AllDestinations,
          navEventNavigator = navigator,
          destinationChangedCallback = { currentRoute = it },
          transitionAnimations = NavHostDefaults.transitionAnimations(
            enterTransition = {
              slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
              )
            },
            exitTransition = {
              slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
              )
            },
            popEnterTransition = {
              slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
              )
            },
            popExitTransition = {
              slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
              )
            },
            replaceEnterTransition = { fadeIn() },
            replaceExitTransition = { fadeOut() },
          ),
        )
      }
    }
  }
}
