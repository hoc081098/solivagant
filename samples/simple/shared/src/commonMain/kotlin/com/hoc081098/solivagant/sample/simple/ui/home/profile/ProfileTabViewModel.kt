package com.hoc081098.solivagant.sample.simple.ui.home.profile

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.safe.safe
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.requireRoute
import com.hoc081098.solivagant.sample.simple.common.COUNTER_KEY
import com.hoc081098.solivagant.sample.simple.common.debugDescription
import com.hoc081098.solivagant.sample.simple.ui.detail.DetailScreenRoute
import io.github.aakira.napier.Napier

internal class ProfileTabViewModel(
  private val navigator: NavEventNavigator,
  private val savedStateHandle: SavedStateHandle,
  intArg: Int,
  stringArg: String,
) : ViewModel() {
  val route = savedStateHandle.requireRoute<ProfileTabRoute>()
  val countStateFlow = savedStateHandle.safe.getStateFlow(COUNTER_KEY)

  init {
    Napier.d("$debugDescription::init with args: $intArg, $stringArg")
    addCloseable { Napier.d("$debugDescription::close") }
  }

  fun increment() = savedStateHandle.safe { it[COUNTER_KEY] = it[COUNTER_KEY] + 1 }

  fun navigateToDetail() = navigator.navigateTo(DetailScreenRoute(id = "id-from-profile"))
}
