package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable

@InternalNavigationApi
public actual class OnBackPressedDispatcherOwner {
  public actual fun addCallback(callback: OnBackPressedCallback) {
    // no-op
  }

  public companion object {
    internal val instance by lazy { OnBackPressedDispatcherOwner() }
  }
}

@InternalNavigationApi
@Composable
public actual fun currentBackPressedDispatcher(): OnBackPressedDispatcherOwner = OnBackPressedDispatcherOwner.instance
