package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.solivagant.navigation.OnBackPressedCallback

internal class DelegatingOnBackPressedCallback : OnBackPressedCallback(false) {

  private val callbacks = mutableListOf<() -> Unit>()

  fun addCallback(callback: () -> Unit) {
    callbacks.add(callback)
    isEnabled = true
  }

  fun removeCallback(callback: () -> Unit) {
    callbacks.remove(callback)
    isEnabled = callbacks.isNotEmpty()
  }

  override fun handleOnBackPressed() {
    callbacks.lastOrNull()?.invoke()
  }
}
