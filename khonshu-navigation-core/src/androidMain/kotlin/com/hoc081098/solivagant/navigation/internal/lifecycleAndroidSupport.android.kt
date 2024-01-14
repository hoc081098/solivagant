package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

// TODO: https://youtrack.jetbrains.com/issue/KT-37316
@Suppress("ACTUAL_WITHOUT_EXPECT") // internal expect is not matched with internal typealias to public type
internal actual typealias LifecycleOwner = androidx.lifecycle.LifecycleOwner

@Composable
@ReadOnlyComposable
internal actual fun currentLifecycleOwner(): LifecycleOwner = LocalLifecycleOwner.current

internal actual suspend fun LifecycleOwner.repeatOnResumeLifecycle(block: suspend () -> Unit) =
  repeatOnLifecycle(state = Lifecycle.State.RESUMED) { block() }
