package com.hoc081098.solivagant.sample.simple.ui.home.feed

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.safe.safe
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.requireRoute
import com.hoc081098.solivagant.sample.simple.common.COUNTER_KEY
import com.hoc081098.solivagant.sample.simple.ui.home.feed.nested_feed.NestedFeedScreenRoute
import io.github.aakira.napier.Napier

internal class FeedTabViewModel(
  private val navigator: NavEventNavigator,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  val route = savedStateHandle.requireRoute<FeedTabRoute>()
  val countStateFlow = savedStateHandle.safe.getStateFlow(COUNTER_KEY)

  init {
    Napier.d("$this::init")
    addCloseable { Napier.d("$this::close") }
  }

  fun navigateToNestedFeed() = navigator.navigateTo(NestedFeedScreenRoute)

  fun increment() = savedStateHandle.safe { it[COUNTER_KEY] = it[COUNTER_KEY] + 1 }
}
