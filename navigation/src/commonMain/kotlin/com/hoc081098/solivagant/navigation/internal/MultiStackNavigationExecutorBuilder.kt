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
  ),
): MultiStackNavigationExecutor {
  // setInputStartRoot must be called before getMultiStackNavigationExecutor
  viewModel.setInputStartRoot(startRoot)

  val lifecycleOwner = LocalLifecycleOwner.current
  val executor = remember(viewModel) {
    viewModel.createMultiStackNavigationExecutor(
      contentDestinations = destinations.filterIsInstance<ContentDestination<*>>(),
      hostLifecycleState = lifecycleOwner.lifecycle.currentState,
    )
  }
  executor.setLifecycleOwner(lifecycleOwner)

  return executor
}
