package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.MainThread

@InternalNavigationApi
public expect abstract class OnBackPressedCallback(enabled: Boolean) {
  public var isEnabled: Boolean

  @MainThread
  public abstract fun handleOnBackPressed()

  @MainThread
  public fun remove()
}
