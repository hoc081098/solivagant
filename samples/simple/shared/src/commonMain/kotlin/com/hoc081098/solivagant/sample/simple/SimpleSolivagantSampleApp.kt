package com.hoc081098.solivagant.sample.simple

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.unit.IntOffset
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.NavHost
import com.hoc081098.solivagant.navigation.NavHostDefaults
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.sample.simple.common.MyApplicationTheme
import com.hoc081098.solivagant.sample.simple.ui.detail.DetailScreenDestination
import com.hoc081098.solivagant.sample.simple.ui.detail.overlay.DetailScreenOverlayDestination
import com.hoc081098.solivagant.sample.simple.ui.home.BottomNavigationInfo
import com.hoc081098.solivagant.sample.simple.ui.home.feed.FeedTabDestination
import com.hoc081098.solivagant.sample.simple.ui.home.feed.nested_feed.NestedFeedScreenDestination
import com.hoc081098.solivagant.sample.simple.ui.home.notifications.NotificationsTabDestination
import com.hoc081098.solivagant.sample.simple.ui.home.notifications.nested_notifications.NestedNotificationsScreenDestination
import com.hoc081098.solivagant.sample.simple.ui.home.profile.ProfileTabDestination
import com.hoc081098.solivagant.sample.simple.ui.login.LoginScreenDestination
import com.hoc081098.solivagant.sample.simple.ui.login.LoginScreenRoute
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Stable
private val AllDestinations: ImmutableSet<NavDestination> = persistentSetOf(
  LoginScreenDestination,
  FeedTabDestination,
  NestedFeedScreenDestination,
  NotificationsTabDestination,
  NestedNotificationsScreenDestination,
  ProfileTabDestination,
  DetailScreenDestination,
  DetailScreenOverlayDestination,
)

@OptIn(
  ExperimentalLayoutApi::class,
  ExperimentalMaterial3Api::class,
)
@Composable
@Suppress("LongMethod")
fun SimpleSolivagantSampleApp(
  modifier: Modifier = Modifier,
  navigator: NavEventNavigator = koinInject(),
) {
  var currentRoute by rememberSaveable { mutableStateOf<BaseRoute?>(null) }

  val onClickNavigationBarItem: (Boolean, BottomNavigationInfo) -> Unit = remember(navigator) {
    {
        isSelected: Boolean, item: BottomNavigationInfo ->
      if (isSelected) {
        // TODO: Handle re-selection of the same tab
      } else {
        // Switch to the new tab if it is not already selected
        navigator.navigateToRoot(
          root = item.root,
          restoreRootState = true,
        )
      }
    }
  }

  val animationSpec = tween<IntOffset>(
    durationMillis = 4_000,
    easing = LinearEasing,
  )

  KoinContext {
    MyApplicationTheme(useDarkTheme = false) {
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
                      imageVector = Icons.Filled.ArrowBack,
                      contentDescription = "Back",
                    )
                  }
                }
              },
            )
          },
          bottomBar = bottomBar@{
            val selectedBottomNavigationInfo = BottomNavigationInfo
              .fromRoute(currentRoute)
              ?: return@bottomBar

            NavigationBar {
              BottomNavigationInfo.ALL.forEach { item ->
                val selected = item.root == selectedBottomNavigationInfo.root

                NavigationBarItem(
                  icon = { Icon(imageVector = item.iconVector, contentDescription = item.title) },
                  label = { Text(item.title) },
                  selected = selected,
                  onClick = { onClickNavigationBarItem(selected, item) },
                )
              }
            }
          },
        ) { innerPadding ->
          NavHost(
            modifier = Modifier.fillMaxSize()
              .padding(innerPadding)
              .consumeWindowInsets(innerPadding),
            startRoute = LoginScreenRoute,
            destinations = AllDestinations,
            navEventNavigator = navigator,
            destinationChangedCallback = { currentRoute = it },
            transitionAnimations = NavHostDefaults.transitionAnimations(
              enterTransition = {
                slideIntoContainer(
                  towards = AnimatedContentTransitionScope.SlideDirection.Left,
                  animationSpec = animationSpec,
                )
              },
              exitTransition = {
                slideOutOfContainer(
                  towards = AnimatedContentTransitionScope.SlideDirection.Left,
                  animationSpec = animationSpec,
                )
              },
              popEnterTransition = {
                slideIntoContainer(
                  towards = AnimatedContentTransitionScope.SlideDirection.Right,
                  animationSpec = animationSpec,
                )
              },
              popExitTransition = {
                slideOutOfContainer(
                  towards = AnimatedContentTransitionScope.SlideDirection.Right,
                  animationSpec = animationSpec,
                )
              },
            ),
          )
        }
      }
    }
  }
}
