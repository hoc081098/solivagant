package com.hoc081098.solivagant.navigation.internal.utils

import android.util.Log

internal actual fun logWarn(
  tag: String,
  message: String,
) {
  Log.w(tag, message)
}
