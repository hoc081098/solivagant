package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

internal expect interface LifecycleOwner

@Composable
@ReadOnlyComposable
internal expect fun currentLifecycleOwner(): LifecycleOwner

internal expect suspend fun LifecycleOwner.repeatOnResumeLifecycle(block: suspend () -> Unit)
