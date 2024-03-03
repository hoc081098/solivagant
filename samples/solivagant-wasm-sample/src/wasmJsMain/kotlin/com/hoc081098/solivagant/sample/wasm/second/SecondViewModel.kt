package com.hoc081098.solivagant.sample.wasm.second

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.navigation.requireRoute

internal class SecondViewModel(
  savedStateHandle: SavedStateHandle,
) : ViewModel() {
  internal val route = savedStateHandle.requireRoute<SecondScreenRoute>()

  init {
    println(">>> $this::init")
    addCloseable { println(">>> $this::close") }
  }
}
