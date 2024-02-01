package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable

@NonRestartableComposable
@Composable
internal expect fun NonAndroidSaveStackState(executor: MultiStackNavigationExecutor)
