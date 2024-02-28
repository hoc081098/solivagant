package com.hoc081098.solivagant.lifecycle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import kotlin.jvm.JvmField

@Composable
public fun rememberLifecycleOwner(lifecycleRegistry: LifecycleRegistry): LifecycleOwner =
  remember(lifecycleRegistry) { LifecycleRegistryOwner(lifecycleRegistry) }

private class LifecycleRegistryOwner(
  @JvmField val lifecycleRegistry: LifecycleRegistry,
) : LifecycleOwner {
  override val lifecycle get() = lifecycleRegistry
}
