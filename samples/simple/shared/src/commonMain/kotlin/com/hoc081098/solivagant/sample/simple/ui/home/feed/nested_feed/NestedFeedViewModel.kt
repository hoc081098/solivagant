package com.hoc081098.solivagant.sample.simple.ui.home.feed.nested_feed

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.navigation.NavEventNavigator
import io.github.aakira.napier.Napier

internal class NestedFeedViewModel(
  private val navigator: NavEventNavigator,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  init {
    Napier.d("$this::init")
    addCloseable { Napier.d("$this::close") }
  }
}
