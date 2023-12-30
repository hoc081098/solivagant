package com.hoc081098.solivagant.navigation

public actual abstract class OnBackPressedCallback actual constructor(enabled: Boolean) {
  public actual var isEnabled: Boolean = enabled
  public actual abstract fun handleOnBackPressed()
}
