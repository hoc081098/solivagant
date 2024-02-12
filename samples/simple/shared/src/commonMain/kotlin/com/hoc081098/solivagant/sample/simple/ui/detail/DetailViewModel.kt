package com.hoc081098.solivagant.sample.simple.ui.detail

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.safe.safe
import com.hoc081098.solivagant.navigation.requireRoute
import com.hoc081098.solivagant.sample.simple.common.COUNTER_KEY
import com.hoc081098.solivagant.sample.simple.common.debugDescription
import com.hoc081098.solivagant.sample.simple.ui.detail.overlay.DetailScreenOverlayRoute
import com.hoc081098.solivagant.sample.simple.ui.home.feed.FeedTabRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class DetailViewModel(
  private val savedStateHandle: SavedStateHandle,
  internal val navigator: DetailNavigator,
) : ViewModel() {
  val route = savedStateHandle.requireRoute<DetailScreenRoute>()
  val countStateFlow = savedStateHandle.safe.getStateFlow(COUNTER_KEY)

  init {
    Napier.d("$debugDescription::init")
    addCloseable { Napier.d("$debugDescription::close") }

    navigator
      .detailScreenResults
      .onEach { Napier.d("$debugDescription::detailScreenResults: value=$it") }
      .launchIn(viewModelScope)
  }

  fun increment() = savedStateHandle.safe { it[COUNTER_KEY] = it[COUNTER_KEY] + 1 }

  fun navigateBack() = navigator.resetToRoot(FeedTabRoute)

  fun showOverlay() = navigator.navigate {
    repeat(10) {
      navigateTo(
        DetailScreenOverlayRoute(
          detailRoute = route,
          id = route.id + "__" + it,
        ),
      )
    }
  }
}
