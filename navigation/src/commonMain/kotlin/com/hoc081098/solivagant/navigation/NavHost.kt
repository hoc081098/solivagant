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
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.compose.LocalSavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.compose.LocalViewModelStoreOwner
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.navigation.internal.MultiStackNavigationExecutor
import com.hoc081098.solivagant.navigation.internal.OnBackPressedCallback
import com.hoc081098.solivagant.navigation.internal.StackEntry
import com.hoc081098.solivagant.navigation.internal.StackEntryId
import com.hoc081098.solivagant.navigation.internal.StackEntryViewModelStoreOwner
import com.hoc081098.solivagant.navigation.internal.StackEvent
import com.hoc081098.solivagant.navigation.internal.VisibleEntryState
import com.hoc081098.solivagant.navigation.internal.WeakReference
import com.hoc081098.solivagant.navigation.internal.currentBackPressedDispatcher
import com.hoc081098.solivagant.navigation.internal.rememberNavigationExecutor
import com.hoc081098.solivagant.navigation.internal.rememberPlatformLifecycleOwner
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
        // From AndroidX:
        // https://github.com/androidx/androidx/blob/5dda4ea48e68d10c8c5cc04d5f4ee299295e1835/navigation/navigation-compose/src/main/java/androidx/navigation/compose/NavHost.kt#L253-L372

        val zIndices = remember { mutableStateMapOf<StackEntryId, Float>() }
        val visibleEntryState by executor.visibleEntries

        val finalEnter: AnimatedContentTransitionScope<*>.(StackEvent) -> EnterTransition = { event ->
          when (event) {
            StackEvent.Idle -> fadeIn(animationSpec = tween(5000, easing = LinearEasing))
            StackEvent.ReplaceAll -> fadeIn(animationSpec = tween(3000, easing = LinearEasing))
            StackEvent.PushRoot -> fadeIn(animationSpec = tween(3000, easing = LinearEasing))
            StackEvent.Push -> transitionAnimations.enterTransition.invoke(this)
            StackEvent.Pop -> transitionAnimations.popEnterTransition.invoke(this)
          }
        }

        val finalExit: AnimatedContentTransitionScope<*>.(StackEvent) -> ExitTransition = { event ->
          when (event) {
            StackEvent.Idle -> fadeOut(animationSpec = tween(5000, easing = LinearEasing))
            StackEvent.ReplaceAll -> fadeOut(animationSpec = tween(3000, easing = LinearEasing))
            StackEvent.PushRoot -> fadeOut(animationSpec = tween(3000, easing = LinearEasing))
            StackEvent.Push -> transitionAnimations.exitTransition.invoke(this)
            StackEvent.Pop -> transitionAnimations.popExitTransition.invoke(this)
          }
        }

        DisposableEffect(Unit) {
          onDispose { executor.removeAllPendingRemovedEntries() }
        }

        val transition: Transition<VisibleEntryState> = updateTransition(
          targetState = visibleEntryState,
          label = "entry",
        )

        @Suppress("RemoveExplicitTypeArguments") // Keep the type for better readability
        transition.AnimatedContent<VisibleEntryState>(
          modifier = Modifier.fillMaxSize(),
          transitionSpec = {
            // A transitionSpec should only use values passed into the `AnimatedContent`, to
            // minimize
            // the transitionSpec recomposing. The states are available as `targetState` and
            // `initialState`.
            val initialId = initialState.currentVisibleEntry.id
            val targetId = targetState.currentVisibleEntry.id
            val lastEvent = targetState.lastEvent

            val initialZIndex = zIndices.getOrPut(initialId) { 0f }

            val targetZIndex = (
              if (targetId == initialId) {
                initialZIndex
              } else {
                when (lastEvent) {
                  StackEvent.Idle -> initialZIndex
                  StackEvent.ReplaceAll -> initialZIndex + 1f
                  StackEvent.Push -> initialZIndex + 1f
                  StackEvent.PushRoot -> initialZIndex + 1f
                  StackEvent.Pop -> initialZIndex - 1f
                }
              }
              ).also { zIndices[targetId] = it }

            println("--> $initialZIndex -> $targetZIndex")

            ContentTransform(
              targetContentEnter = finalEnter(lastEvent),
              initialContentExit = finalExit(lastEvent),
              targetContentZIndex = targetZIndex,
            ).using(
              // Disable clipping since the faded slide-in/out should
              // be displayed out of bounds.
              SizeTransform(clip = false),
            )
          },
          contentAlignment = Alignment.Center,
          contentKey = { it.currentVisibleEntry.id },
        ) { targetState ->
          // while in the scope of the composable, we provide the ViewModelStoreOwner and LifecycleOwner
          val currentEntry = targetState.currentVisibleEntry

          Show(
            modifier = Modifier.fillMaxSize(),
            entry = currentEntry,
            executor = executor,
            saveableStateHolder = saveableStateHolder,
            canMoveToResumed = false,
          )

          DisposableEffect(currentEntry) {
            onDispose {
              println("### onDispose: $currentEntry")
              executor.removeEntryIfNeeded(currentEntry)
            }
          }
        }

        // Mark transition complete when the transition is finished
        LaunchedEffect(executor, transition.currentState, transition.targetState) {
          if (transition.currentState == transition.targetState) {
            println(
              ">>> Move to RESUMED ${
                transition
                  .targetState
                  .currentVisibleEntry
                  .route
              }",
            )

            transition
              .targetState
              .currentVisibleEntry
              .lifecycleOwner.maxLifecycle = Lifecycle.State.RESUMED

            transition
              .targetState
              .previousVisibleEntry
              ?.let { entry ->
                println("### LaunchedEffect: previousVisibleEntry=$entry")
                executor.removeEntryIfNeeded(entry)
              }

            zIndices
              .filter { it.key != transition.targetState.currentVisibleEntry.id }
              .forEach { zIndices.remove(it.key) }

            println(zIndices.toMap())
          }
        }

        visibleEntryState.visibleEntries.forEachIndexed { index, entry ->
          // Skip the first entry because it's already shown by AnimatedContent
          if (index > 0) {
            Show(
              modifier = Modifier.matchParentSize(),
              entry = entry,
              executor = executor,
              saveableStateHolder = saveableStateHolder,
              canMoveToResumed = true,
            )
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
  canMoveToResumed: Boolean,
  modifier: Modifier = Modifier,
) {
  // Remember the `saveableCloseable` and `savedStateHandleFactory`
  // because this composition can live longer than
  // the entry is present in the backstack, if transition is enabled for example.

  // From AndroidX Navigation:
  //   Stash a reference to the SaveableStateHolder in the Store so that
  //   it is available when the destination is cleared. Which, because of animations,
  //   only happens after this leaves composition. Which means we can't rely on
  //   DisposableEffect to clean up this reference (as it'll be cleaned up too early)
  val saveableCloseable = remember(entry.id, executor, saveableStateHolder) {
    executor
      .storeFor(entry.id)
      .getOrCreate(SaveableCloseable::class) {
        SaveableCloseable(
          entry.id.value,
          WeakReference(saveableStateHolder),
        )
      }
  }

  val savedStateHandleFactory = remember(executor, entry.id) {
    SavedStateHandleFactory { executor.savedStateHandleFor(entry.id) }
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
    entry.lifecycleOwner.maxLifecycle = if (canMoveToResumed) {
      Lifecycle.State.RESUMED
    } else {
      Lifecycle.State.STARTED
    }
    onDispose { entry.lifecycleOwner.maxLifecycle = Lifecycle.State.CREATED }
  }

  CompositionLocalProvider(
    LocalViewModelStoreOwner provides viewModelStoreOwner,
    LocalSavedStateHandleFactory provides savedStateHandleFactory,
    LocalLifecycleOwner provides entry.lifecycleOwner,
  ) {
    saveableStateHolder.SaveableStateProvider(entry.id.value) {
      entry.destination.content(entry.route, modifier)
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
        .map { it.visibleEntries.last().route }
        .distinctUntilChanged()
        .collect { destinationChangedCallback(it) }
    }
  }
}
