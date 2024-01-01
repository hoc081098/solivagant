package com.hoc081098.solivagant.navigation.internal

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@InternalNavigationApi
public actual class OnBackPressedDispatcherOwner(
  private val androidXOwner: androidx.activity.OnBackPressedDispatcherOwner,
) {
  public actual fun addCallback(callback: OnBackPressedCallback): Unit =
    androidXOwner.onBackPressedDispatcher.addCallback(callback)
}

@InternalNavigationApi
@Composable
public actual fun currentBackPressedDispatcher(): OnBackPressedDispatcherOwner {
  val onBackPressedDispatcherOwner = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
    "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
  }
  return remember(onBackPressedDispatcherOwner) { OnBackPressedDispatcherOwner(onBackPressedDispatcherOwner) }
}
