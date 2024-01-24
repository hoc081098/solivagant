package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hoc081098.kmp.viewmodel.compose.kmpViewModel
import com.hoc081098.kmp.viewmodel.createSavedStateHandle
import com.hoc081098.kmp.viewmodel.viewModelFactory
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavRoot
import kotlinx.collections.immutable.ImmutableSet

@Composable
internal fun rememberNavigationExecutor(
  startRoot: NavRoot,
  destinations: ImmutableSet<NavDestination>,
  viewModel: StoreViewModel = kmpViewModel(
    viewModelFactory {
      StoreViewModel(
        globalSavedStateHandle = createSavedStateHandle(),
      )
    },
  )
): MultiStackNavigationExecutor {
  val lifecycleOwner = LocalLifecycleOwner.current

  viewModel.setInputStartRoot(startRoot)

  val executor = remember(viewModel) {
    val contentDestinations = destinations.filterIsInstance<ContentDestination<*>>()

    val navState = viewModel.getSavedStackState()
    val hostLifecycleState = lifecycleOwner.lifecycle.currentState

    val stack = if (navState == null) {
      MultiStack.createWith(
        root = viewModel.savedNavRoot!!,
        destinations = contentDestinations,
        hostLifecycleState = hostLifecycleState,
        onStackEntryRemoved = viewModel::removeEntry,
      )
    } else {
      MultiStack.fromState(
        root = viewModel.savedNavRoot!!,
        bundle = navState,
        destinations = contentDestinations,
        hostLifecycleState = hostLifecycleState,
        onStackEntryRemoved = viewModel::removeEntry,
      )
    }

    @Suppress("ViewModelForwarding")
    MultiStackNavigationExecutor(
      stack = stack,
      viewModel = viewModel,
      onRootChanged = viewModel::setStartRoot,
    )
  }

  executor.setLifecycleOwner(lifecycleOwner)

  return executor
}
