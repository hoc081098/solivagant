package com.hoc081098.solivagant.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.kmp.viewmodel.compose.SavedStateHandleFactoryProvider
import com.hoc081098.kmp.viewmodel.compose.ViewModelStoreOwnerProvider
import com.hoc081098.solivagant.navigation.internal.MultiStackNavigationExecutor
import com.hoc081098.solivagant.navigation.internal.OnBackPressedCallback
import com.hoc081098.solivagant.navigation.internal.StackEntry
import com.hoc081098.solivagant.navigation.internal.ViewModelStoreOwnerCloseable
import com.hoc081098.solivagant.navigation.internal.WeakReference
import com.hoc081098.solivagant.navigation.internal.currentBackPressedDispatcher
import com.hoc081098.solivagant.navigation.internal.rememberNavigationExecutor
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Create a new `NavHost` containing all given [destinations]. [startRoute] will be used as the
 * start destination of the graph. Use [com.hoc081098.solivagant.navigation.NavEventNavigator] and
 * [NavigationSetup] to change what is shown in [NavHost].
 *
 * If a [NavEventNavigator] is passed it will be automatically set up and can be used to
 * navigate within the `NavHost`.
 */
@Composable
public fun NavHost(
  startRoute: NavRoot,
  destinations: ImmutableSet<NavDestination>,
  modifier: Modifier = Modifier,
  navEventNavigator: NavEventNavigator? = null,
  destinationChangedCallback: ((BaseRoute) -> Unit)? = null,
) {
  val executor = rememberNavigationExecutor(startRoute, destinations)

  SystemBackHandling(executor)
  DestinationChangedCallback(executor, destinationChangedCallback)

  val saveableStateHolder = rememberSaveableStateHolder()
  CompositionLocalProvider(LocalNavigationExecutor provides executor) {
    if (navEventNavigator != null) {
      NavigationSetup(navEventNavigator)
    }

    Box(modifier = modifier) {
      executor.visibleEntries.value.forEach { entry ->
        Show(
          entry = entry,
          executor = executor,
          saveableStateHolder = saveableStateHolder,
        )
      }
    }
  }
}

@Composable
private fun <T : BaseRoute> Show(
  entry: StackEntry<T>,
  executor: MultiStackNavigationExecutor,
  saveableStateHolder: SaveableStateHolder,
) {
  // From AndroidX Navigation:
  //   Stash a reference to the SaveableStateHolder in the Store so that
  //   it is available when the destination is cleared. Which, because of animations,
  //   only happens after this leaves composition. Which means we can't rely on
  //   DisposableEffect to clean up this reference (as it'll be cleaned up too early)
  remember(entry, executor, saveableStateHolder) {
    executor.storeFor(entry.id).getOrCreate(SaveableCloseable::class) {
      SaveableCloseable(entry.id.value, WeakReference(saveableStateHolder))
    }
  }

  saveableStateHolder.SaveableStateProvider(entry.id.value) {
    ViewModelStoreOwnerProvider(
      viewModelStoreOwner = executor
        .storeFor(entry.id)
        .getOrCreate(
          ViewModelStoreOwnerCloseable::class,
          ::ViewModelStoreOwnerCloseable,
        ),
    ) {
      SavedStateHandleFactoryProvider(
        savedStateHandleFactory = { executor.savedStateHandleFor(entry.destinationId) },
      ) {
        entry.destination.content(entry.route)
      }
    }
  }
}

internal class SaveableCloseable(
  private val id: String,
  private val saveableStateHolderRef: WeakReference<SaveableStateHolder>,
) : Closeable {
  override fun close() {
    saveableStateHolderRef.get()?.removeState(id)
    saveableStateHolderRef.clear()
  }
}

@Composable
private fun SystemBackHandling(executor: MultiStackNavigationExecutor) {
  val backPressedDispatcher = currentBackPressedDispatcher()

  val callback = remember(executor) {
    object : OnBackPressedCallback(executor.canNavigateBack.value) {
      override fun handleOnBackPressed() {
        executor.navigateBack()
      }
    }
  }

  LaunchedEffect(executor, callback) {
    snapshotFlow { executor.canNavigateBack.value }
      .collect { callback.isEnabled = it }
  }

  DisposableEffect(backPressedDispatcher, callback) {
    backPressedDispatcher.addCallback(callback)

    onDispose {
      callback.remove()
    }
  }
}

@Composable
private fun DestinationChangedCallback(
  executor: MultiStackNavigationExecutor,
  destinationChangedCallback: ((BaseRoute) -> Unit)?,
) {
  if (destinationChangedCallback != null) {
    LaunchedEffect(executor, destinationChangedCallback) {
      snapshotFlow { executor.visibleEntries.value }
        .map { it.last().route }
        .distinctUntilChanged()
        .collect { destinationChangedCallback(it) }
    }
  }
}
