package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.InternalKmpViewModelApi
import com.hoc081098.kmp.viewmodel.ViewModelStore

@OptIn(InternalKmpViewModelApi::class)
internal actual fun createViewModelStore(): ViewModelStore =
  ViewModelStore(androidx.lifecycle.ViewModelStore())
