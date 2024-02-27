package com.hoc081098.solivagant.navigation

import androidx.compose.ui.uikit.ComposeUIViewControllerDelegate
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.navigation.internal.LifecycleOwnerComposeUIViewControllerDelegateImpl

/**
 * Represents both a [ComposeUIViewControllerDelegate] and a [LifecycleOwner].
 */
public sealed interface LifecycleOwnerComposeUIViewControllerDelegate :
  ComposeUIViewControllerDelegate,
  LifecycleOwner

@Suppress("FunctionName") // Factory function
public fun LifecycleOwnerComposeUIViewControllerDelegate(hostLifecycleOwner: LifecycleOwner): LifecycleOwnerComposeUIViewControllerDelegate =
  LifecycleOwnerComposeUIViewControllerDelegateImpl(hostLifecycleOwner)

@Suppress("FunctionName") // Factory function
public fun LifecycleOwnerComposeUIViewControllerDelegate(): LifecycleOwnerComposeUIViewControllerDelegate =
  LifecycleOwnerComposeUIViewControllerDelegateImpl(null)
