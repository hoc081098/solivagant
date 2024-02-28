package com.hoc081098.solivagant.navigation

import androidx.compose.ui.uikit.ComposeUIViewControllerDelegate
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner

/**
 * Represents both a [ComposeUIViewControllerDelegate] and a [LifecycleOwner].
 */
public sealed interface LifecycleOwnerComposeUIViewControllerDelegate :
  ComposeUIViewControllerDelegate,
  LifecycleOwner {
  /**
   * Move to [Lifecycle.State.DESTROYED].
   */
  public fun onDestroy()
}

/**
 * Bind [LifecycleOwnerComposeUIViewControllerDelegate] to [SavedStateSupport].
 * When [SavedStateSupport.clear] is called, we move the lifecycle to [Lifecycle.State.DESTROYED].
 */
public fun LifecycleOwnerComposeUIViewControllerDelegate.bindTo(savedStateSupport: SavedStateSupport): Unit =
  savedStateSupport.addCloseable(key = this) { onDestroy() }

@Suppress("FunctionName") // Factory function
public fun LifecycleOwnerComposeUIViewControllerDelegate(
  hostLifecycleOwner: LifecycleOwner,
): LifecycleOwnerComposeUIViewControllerDelegate =
  LifecycleOwnerComposeUIViewControllerDelegateImpl(hostLifecycleOwner)

@Suppress("FunctionName") // Factory function
public fun LifecycleOwnerComposeUIViewControllerDelegate(): LifecycleOwnerComposeUIViewControllerDelegate =
  LifecycleOwnerComposeUIViewControllerDelegateImpl(null)
