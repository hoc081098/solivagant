package com.hoc081098.solivagant.sample.wasm.start

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.navigation.requireRoute

internal class StartViewModel(
  savedStateHandle: SavedStateHandle,
) : ViewModel() {
  internal val route = savedStateHandle.requireRoute<StartScreenRoute>()

  init {
    println(">>> $this::init")
    addCloseable { println(">>> $this::close") }
  }
}
