package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable

@NonRestartableComposable
@Composable
internal actual fun NonAndroidSaveStackState(executor: MultiStackNavigationExecutor) {
  // No-op
}
