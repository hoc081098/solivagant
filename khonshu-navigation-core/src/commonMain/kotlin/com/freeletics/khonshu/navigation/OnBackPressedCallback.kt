package com.freeletics.khonshu.navigation

public expect abstract class OnBackPressedCallback constructor(isEnabled: Boolean) {
  public var isEnabled: Boolean

  public abstract fun handleOnBackPressed()
}
