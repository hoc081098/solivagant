package com.freeletics.khonshu.navigation.internal

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hoc081098.solivagant.navigation.OnBackPressedCallback

internal actual class OnBackPressedDispatcherOwner(
  private val androidXOwner: androidx.activity.OnBackPressedDispatcherOwner,
) {
  actual fun addCallback(callback: OnBackPressedCallback) =
    androidXOwner.onBackPressedDispatcher.addCallback(callback)
}

@Composable
internal actual fun currentBackPressedDispatcher(): OnBackPressedDispatcherOwner {
  val onBackPressedDispatcherOwner = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
    "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
  }
  return remember(onBackPressedDispatcherOwner) { OnBackPressedDispatcherOwner(onBackPressedDispatcherOwner) }
}
