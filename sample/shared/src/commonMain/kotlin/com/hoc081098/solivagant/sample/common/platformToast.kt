package com.hoc081098.solivagant.sample.common

import androidx.compose.runtime.Stable

@Stable
expect class PlatformToastManager() {
  fun showToast(message: String)
}
