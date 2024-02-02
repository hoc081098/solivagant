package com.hoc081098.solivagant.sample.simple.ui.login

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.safe.NonNullSavedStateHandleKey
import com.hoc081098.kmp.viewmodel.safe.int
import com.hoc081098.kmp.viewmodel.safe.safe
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.navigation.requireRoute
import com.hoc081098.solivagant.sample.simple.ui.home.feed.FeedTabRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

internal class LoginViewModel(
  private val navigator: NavEventNavigator,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  val route = savedStateHandle.requireRoute<LoginScreenRoute>()
  val countStateFlow = savedStateHandle.safe.getStateFlow(COUNTER_KEY)

  init {
    Napier.d("$debugDescription::init")
    addCloseable { Napier.d("$debugDescription::close") }
  }

  fun login() {
    viewModelScope.launch {
      // TODO: Save auth state
      navigator.replaceAll(FeedTabRoute)
    }
  }

  fun increment() {
    savedStateHandle.safe { it[COUNTER_KEY] = it[COUNTER_KEY] + 1 }
  }

  private companion object {
    val COUNTER_KEY = NonNullSavedStateHandleKey.int("count", 0)
  }
}
