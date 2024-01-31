package com.hoc081098.solivagant.navigation.core

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.hoc081098.solivagant.navigation.core.internal.DefaultNavigator

@Composable
public actual fun BackHandler(navigator: Navigator) {
  navigator as DefaultNavigator

  val backPressedDispatcherOwner = requireNotNull(LocalOnBackPressedDispatcherOwner.current) {
    "No OnBackPressedDispatcher available"
  }
  val lifecycleOwner = LocalLifecycleOwner.current

  val callback = remember(navigator) {
    object : OnBackPressedCallback(enabled = navigator.canNavigateBackState.value) {
      override fun handleOnBackPressed() = navigator.navigateBack()
    }
  }

  LaunchedEffect(callback, navigator) {
    snapshotFlow { navigator.canNavigateBackState.value }
      .collect { callback.isEnabled = it }
  }

  DisposableEffect(backPressedDispatcherOwner, callback, lifecycleOwner) {
    backPressedDispatcherOwner.onBackPressedDispatcher.addCallback(
      lifecycleOwner,
      callback,
    )
    onDispose(callback::remove)
  }
}
