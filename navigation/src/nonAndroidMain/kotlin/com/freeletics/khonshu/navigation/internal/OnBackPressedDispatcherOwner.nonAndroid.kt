package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Composable
import com.hoc081098.solivagant.navigation.OnBackPressedCallback

internal actual class OnBackPressedDispatcherOwner {
  actual fun addCallback(callback: OnBackPressedCallback) {
    // no-op
  }

  companion object {
    internal val instance by lazy { OnBackPressedDispatcherOwner() }
  }
}

@Composable
internal actual fun currentBackPressedDispatcher(): OnBackPressedDispatcherOwner = OnBackPressedDispatcherOwner.instance
