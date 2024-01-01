package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Composable
import com.hoc081098.solivagant.navigation.OnBackPressedCallback

internal expect class OnBackPressedDispatcherOwner {
  fun addCallback(callback: OnBackPressedCallback)
}

@Composable
internal expect fun currentBackPressedDispatcher(): OnBackPressedDispatcherOwner
