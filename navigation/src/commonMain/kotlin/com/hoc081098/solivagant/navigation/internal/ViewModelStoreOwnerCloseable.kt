package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.kmp.viewmodel.ViewModelStore
import com.hoc081098.kmp.viewmodel.ViewModelStoreOwner

internal expect fun createViewModelStore(): ViewModelStore

internal class ViewModelStoreOwnerCloseable : Closeable, ViewModelStoreOwner {
  private val viewModelStoreLazy = lazy { createViewModelStore() }

  override fun close() {
    if (viewModelStoreLazy.isInitialized()) {
      viewModelStoreLazy.value.clear()
    }
  }

  override val viewModelStore: ViewModelStore by viewModelStoreLazy
}
