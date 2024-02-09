/*
 * Copyright 2021 Freeletics GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2024 Petrus Nguyễn Thái Học
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hoc081098.solivagant.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.kmp.viewmodel.compose.LocalSavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.compose.LocalViewModelStoreOwner
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.navigation.internal.MultiStackNavigationExecutor
import com.hoc081098.solivagant.navigation.internal.OnBackPressedCallback
import com.hoc081098.solivagant.navigation.internal.StackEntry
import com.hoc081098.solivagant.navigation.internal.StackEntryId
import com.hoc081098.solivagant.navigation.internal.StackEntryViewModelStoreOwner
import com.hoc081098.solivagant.navigation.internal.WeakReference
import com.hoc081098.solivagant.navigation.internal.currentBackPressedDispatcher
import com.hoc081098.solivagant.navigation.internal.rememberNavigationExecutor
import com.hoc081098.solivagant.navigation.internal.rememberPlatformLifecycleOwner
import kotlinx.collections.immutable.ImmutableList
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
  contentAlignment: Alignment = Alignment.Center,
  transitionAnimations: NavHostTransitionAnimations = NavHostDefaults.transitionAnimations(),
) {
  val lifecycleOwner = rememberPlatformLifecycleOwner()

  CompositionLocalProvider(
    LocalLifecycleOwner providesDefault lifecycleOwner,
  ) {
    val executor = rememberNavigationExecutor(
      startRoot = startRoute,
      destinations = destinations,
    )

    SystemBackHandling(executor)
    DestinationChangedCallback(executor, destinationChangedCallback)

    val saveableStateHolder = rememberSaveableStateHolder()

    CompositionLocalProvider(
      LocalNavigationExecutor provides executor,
    ) {
      if (navEventNavigator != null) {
        NavigationSetup(navEventNavigator)
      }

      Box(modifier = modifier) {
        val zIndices = remember { mutableMapOf<StackEntryId, Float>() }

        val visibleEntries: ImmutableList<StackEntry<*>> = executor.visibleEntries.value
        val visibleStackEntry: StackEntry<*>? = visibleEntries.lastOrNull()

        if (visibleStackEntry != null) {
          val isPopState = executor.isPop

          val finalEnter: AnimatedContentTransitionScope<StackEntry<*>>.() -> EnterTransition = {
            if (isPopState.value) {
              transitionAnimations.popEnterTransition.invoke(this)
            } else {
              transitionAnimations.enterTransition.invoke(this)
            }
          }

          val finalExit: AnimatedContentTransitionScope<StackEntry<*>>.() -> ExitTransition = {
            if (isPopState.value) {
              transitionAnimations.popExitTransition.invoke(this)
            } else {
              transitionAnimations.exitTransition.invoke(this)
            }
          }

          DisposableEffect(Unit) {
            onDispose {
              visibleEntries.forEach { entry ->
                executor.onTransitionComplete(entry)
              }
            }
          }

          val transition: Transition<StackEntry<*>> = updateTransition(
            targetState = visibleStackEntry,
            label = "entry",
          )

          transition.AnimatedContent<StackEntry<*>>(
            modifier = modifier,
            transitionSpec = {
              // If the initialState of the AnimatedContent is not in visibleEntries, we are in
              // a case where visible has cleared the old state for some reason, so instead of
              // attempting to animate away from the initialState, we skip the animation.
              if (initialState in visibleEntries) {
                val initialZIndex = zIndices[initialState.id]
                  ?: 0f.also { zIndices[initialState.id] = 0f }
                val targetZIndex = when {
                  targetState.id == initialState.id -> initialZIndex
                  isPopState.value -> initialZIndex - 1f
                  else -> initialZIndex + 1f
                }.also { zIndices[targetState.id] = it }

                ContentTransform(finalEnter(this), finalExit(this), targetZIndex)
              } else {
                EnterTransition.None togetherWith ExitTransition.None
              }
            },
            contentAlignment = contentAlignment,
            contentKey = { it.id },
          ) { targetState ->
            // In some specific cases, such as clearing your back stack by changing your
            // start destination, AnimatedContent can contain an entry that is no longer
            // part of visible entries since it was cleared from the back stack and is not
            // animating. In these cases the currentEntry will be null, and in those cases,
            // AnimatedContent will just skip attempting to transition the old entry.
            // See https://issuetracker.google.com/238686802
            val currentEntry = visibleEntries.lastOrNull { entry -> targetState == entry }

            // while in the scope of the composable, we provide the ViewModelStoreOwner and LifecycleOwner
            currentEntry?.let {
              Show(
                entry = it,
                executor = executor,
                saveableStateHolder = saveableStateHolder,
              )
            }
          }

          LaunchedEffect(transition.currentState, transition.targetState) {
            if (transition.currentState == transition.targetState) {
              visibleEntries.forEach { entry ->
                executor.onTransitionComplete(entry)
              }
              zIndices
                .filter { it.key != transition.targetState.id }
                .forEach { zIndices.remove(it.key) }
            }
          }
        }
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
  val saveableCloseable = remember(entry, executor, saveableStateHolder) {
    executor
      .storeFor(entry.id)
      .getOrCreate(SaveableCloseable::class) {
        SaveableCloseable(
          entry.id.value,
          WeakReference(saveableStateHolder),
        )
      }
  }

  val viewModelStoreOwner = saveableCloseable
    .viewModelStoreOwnerState
    .value // <-- This will cause the recomposition when the value is cleared.
    ?: return

  // We already checked that viewModelStoreOwner is not null (meaning the entry has not been removed).
  // So, we can safely update entry.lifecycleOwner.maxLifecycle.
  //
  // Previously, we have checked `entry.lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED,
  // but there was an issue on Android platform, the entry state was moved to DESTROYED (due to configuration change)
  // but it was not removed from the stack.
  DisposableEffect(entry) {
    entry.lifecycleOwner.maxLifecycle = Lifecycle.State.RESUMED
    onDispose { entry.lifecycleOwner.maxLifecycle = Lifecycle.State.CREATED }
  }

  CompositionLocalProvider(
    LocalViewModelStoreOwner provides viewModelStoreOwner,
    LocalSavedStateHandleFactory provides executor.savedStateHandleFactoryFor(entry.id),
    LocalLifecycleOwner provides entry.lifecycleOwner,
  ) {
    saveableStateHolder.SaveableStateProvider(entry.id.value) {
      entry.destination.content(entry.route)
    }
  }
}

internal class SaveableCloseable(
  private val id: String,
  private val saveableStateHolderRef: WeakReference<SaveableStateHolder>,
) : Closeable {
  private val _viewModelStoreOwnerState: MutableState<StackEntryViewModelStoreOwner?> =
    mutableStateOf(StackEntryViewModelStoreOwner())

  inline val viewModelStoreOwnerState: State<StackEntryViewModelStoreOwner?> get() = _viewModelStoreOwnerState

  override fun close() {
    Snapshot.withMutableSnapshot {
      _viewModelStoreOwnerState.value?.clearIfInitialized()
      _viewModelStoreOwnerState.value = null
    }

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
