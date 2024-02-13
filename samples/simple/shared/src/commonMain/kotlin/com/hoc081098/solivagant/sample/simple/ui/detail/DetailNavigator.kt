package com.hoc081098.solivagant.sample.simple.ui.detail

import com.hoc081098.kmp.viewmodel.parcelable.Parcelable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavEventNavigator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow

@Parcelize
data class DetailScreenResult(val value: String) : Parcelable

class DetailNavigator : NavEventNavigator() {
  init {
    Napier.d(message = "$this::init")
  }

  private val detailScreenResultRequest = registerForNavigationResult<DetailScreenRoute, DetailScreenResult>()

  val detailScreenResults: Flow<DetailScreenResult> = detailScreenResultRequest.results

  fun deliverResultToDetailScreen(value: String) {
    deliverNavigationResult(detailScreenResultRequest.key, DetailScreenResult(value))
  }
}
