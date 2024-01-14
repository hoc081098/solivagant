package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

internal actual interface LifecycleOwner {
  companion object : LifecycleOwner
}

@Composable
@ReadOnlyComposable
internal actual fun currentLifecycleOwner(): LifecycleOwner = LifecycleOwner

internal actual suspend fun LifecycleOwner.repeatOnResumeLifecycle(block: suspend () -> Unit) = block()
