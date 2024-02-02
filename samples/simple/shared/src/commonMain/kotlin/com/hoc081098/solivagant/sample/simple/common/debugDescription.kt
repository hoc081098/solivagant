package com.hoc081098.solivagant.sample.simple.common

import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.safe.NonNullSavedStateHandleKey
import com.hoc081098.kmp.viewmodel.safe.int
import kotlin.jvm.JvmField

internal inline val ViewModel.debugDescription: String
  get() = toString().substringAfterLast('.')

@JvmField
internal val COUNTER_KEY = NonNullSavedStateHandleKey.int("count", 0)
