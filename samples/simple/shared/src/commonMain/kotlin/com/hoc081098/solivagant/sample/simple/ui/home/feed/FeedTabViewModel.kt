package com.hoc081098.solivagant.sample.simple.ui.home.feed

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.sample.simple.ui.home.feed.nested_feed.NestedFeedScreenRoute
import io.github.aakira.napier.Napier

internal class FeedTabViewModel(
  private val navigator: NavEventNavigator,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  init {
    Napier.d("$this::init")
    addCloseable { Napier.d("$this::close") }
  }

  fun navigateToNestedFeed() = navigator.navigateTo(NestedFeedScreenRoute)
}
