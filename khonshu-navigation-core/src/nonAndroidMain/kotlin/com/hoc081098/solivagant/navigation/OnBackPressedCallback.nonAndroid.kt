package com.hoc081098.solivagant.navigation

import com.hoc081098.kmp.viewmodel.MainThread

public actual abstract class OnBackPressedCallback actual constructor(enabled: Boolean) {
  public actual var isEnabled: Boolean = enabled

  @MainThread
  public actual abstract fun handleOnBackPressed()

  @MainThread
  public actual fun remove() {
    // no-op
  }
}
