package com.hoc081098.solivagant.sample.simple.ui.detail

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.safe.safe
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.requireRoute
import com.hoc081098.solivagant.sample.simple.common.COUNTER_KEY
import com.hoc081098.solivagant.sample.simple.common.debugDescription
import io.github.aakira.napier.Napier

internal class DetailViewModel(
  private val savedStateHandle: SavedStateHandle,
  private val navigator: NavEventNavigator,
) : ViewModel() {
  val route = savedStateHandle.requireRoute<DetailScreenRoute>()
  val countStateFlow = savedStateHandle.safe.getStateFlow(COUNTER_KEY)

  init {
    Napier.d("$debugDescription::init")
    addCloseable { Napier.d("$debugDescription::close") }
  }

  fun increment() = savedStateHandle.safe { it[COUNTER_KEY] = it[COUNTER_KEY] + 1 }

  fun navigateBack() = navigator.navigateBack()
}