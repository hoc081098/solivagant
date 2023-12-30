package com.hoc081098.solivagant.navigation

public expect abstract class OnBackPressedCallback(enabled: Boolean) {
  public var isEnabled: Boolean

  public abstract fun handleOnBackPressed()
}
