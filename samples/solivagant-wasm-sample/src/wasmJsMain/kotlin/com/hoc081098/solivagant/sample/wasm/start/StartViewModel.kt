package com.hoc081098.solivagant.sample.wasm.start

import com.hoc081098.flowext.interval
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.navigation.requireRoute
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

internal class StartViewModel(
  savedStateHandle: SavedStateHandle,
) : ViewModel() {
  internal val route = savedStateHandle.requireRoute<StartScreenRoute>()

  internal val timerFlow: StateFlow<Long?> = interval(Duration.ZERO, 1.seconds)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(@Suppress("MagicNumber") 5_000),
      initialValue = null,
    )

  init {
    println(">>> $this::init")
    addCloseable { println(">>> $this::close") }
  }
}
