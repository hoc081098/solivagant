package com.hoc081098.solivagant.sample.simple.ui.detail.overlay

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hoc081098.solivagant.navigation.NavigationSetup
import com.hoc081098.solivagant.navigation.Navigator.Companion.navigateBackTo
import com.hoc081098.solivagant.sample.simple.ui.detail.DetailNavigator
import com.hoc081098.solivagant.sample.simple.ui.detail.DetailScreenRoute
import org.koin.compose.koinInject

@Composable
internal fun DetailScreenOverlay(
  route: DetailScreenOverlayRoute,
  modifier: Modifier = Modifier,
) {
  val navigator = koinInject<DetailNavigator>()

  NavigationSetup(navigator)

  AlertDialog(
    modifier = modifier,
    onDismissRequest = navigator::navigateBack,
    title = { Text(text = route.toString()) },
    text = { Text(text = "This is an overlay") },
    dismissButton = {
      TextButton(onClick = navigator::navigateBack) {
        Text(text = "Back")
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          navigator.deliverResultToDetailScreen("Result from overlay $route")
          navigator.navigateBackTo<DetailScreenRoute>(inclusive = false)
        },
      ) {
        Text(text = "Back to detail")
      }
    },
  )
}
