package com.hoc081098.solivagant.sample.simple

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.NavHost
import com.hoc081098.solivagant.sample.simple.common.MyApplicationTheme
import com.hoc081098.solivagant.sample.simple.ui.home.BottomNavigationInfo
import com.hoc081098.solivagant.sample.simple.ui.home.feed.FeedTabDestination
import com.hoc081098.solivagant.sample.simple.ui.home.feed.nested_feed.NestedFeedScreenDestination
import com.hoc081098.solivagant.sample.simple.ui.home.notifications.NotificationsTabDestination
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
  ProfileTabDestination,
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
                IconButton(
                  onClick = {
                    navigator.navigateBack()
                  },
                ) {
                  Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                  )
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
                NavigationBarItem(
                  icon = { Icon(imageVector = item.iconVector, contentDescription = item.title) },
                  label = { Text(item.title) },
                  selected = item.root == selectedBottomNavigationInfo.root,
                  onClick = {
                    navigator.navigateToRoot(
                      root = item.root,
                      restoreRootState = true,
                    )
                  },
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
          )
        }
      }
    }
  }
}
