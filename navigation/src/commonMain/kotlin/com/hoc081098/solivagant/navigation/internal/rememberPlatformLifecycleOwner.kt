package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import com.hoc081098.solivagant.lifecycle.LifecycleOwner

@Composable
internal expect fun rememberPlatformLifecycleOwner(): LifecycleOwner
