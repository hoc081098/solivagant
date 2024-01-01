package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hoc081098.kmp.viewmodel.compose.kmpViewModel
import com.hoc081098.kmp.viewmodel.createSavedStateHandle
import com.hoc081098.kmp.viewmodel.viewModelFactory
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavRoot

@Composable
internal fun rememberNavigationExecutor(
  startRoot: NavRoot,
  destinations: Set<NavDestination>,
): MultiStackNavigationExecutor {
  val viewModel = kmpViewModel(
    viewModelFactory {
      StoreViewModel(
        globalSavedStateHandle = createSavedStateHandle(),
      )
    },
  )
  viewModel.setInputStartRoot(startRoot)

  return remember(viewModel) {
    val contentDestinations = destinations.filterIsInstance<ContentDestination<*>>()

    val navState = viewModel.getSavedStackState()
    val stack = if (navState == null) {
      MultiStack.createWith(
        root = viewModel.savedNavRoot!!,
        destinations = contentDestinations,
        onStackEntryRemoved = viewModel::removeEntry,
      )
    } else {
      MultiStack.fromState(
        root = viewModel.savedNavRoot!!,
        bundle = navState,
        destinations = contentDestinations,
        onStackEntryRemoved = viewModel::removeEntry,
      )
    }

    MultiStackNavigationExecutor(
      stack = stack,
      viewModel = viewModel,
      onRootChanged = viewModel::setStartRoot,
    )
  }
}
