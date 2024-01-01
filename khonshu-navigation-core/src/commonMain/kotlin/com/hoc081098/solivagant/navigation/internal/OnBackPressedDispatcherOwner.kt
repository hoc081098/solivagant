package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable

@InternalNavigationApi
public expect class OnBackPressedDispatcherOwner {
  public fun addCallback(callback: OnBackPressedCallback)
}

@InternalNavigationApi
@Composable
public expect fun currentBackPressedDispatcher(): OnBackPressedDispatcherOwner
