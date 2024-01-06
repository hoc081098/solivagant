package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.ViewModelStore
import com.hoc081098.kmp.viewmodel.ViewModelStoreOwner
import kotlin.LazyThreadSafetyMode.NONE

internal expect fun createViewModelStore(): ViewModelStore

internal class StackEntryViewModelStoreOwner : ViewModelStoreOwner {
  private val viewModelStoreLazy = lazy(NONE) { createViewModelStore() }

  fun clearIfInitialized() {
    if (viewModelStoreLazy.isInitialized()) {
      viewModelStoreLazy.value.clear()
    }
  }

  override val viewModelStore: ViewModelStore by viewModelStoreLazy
}
