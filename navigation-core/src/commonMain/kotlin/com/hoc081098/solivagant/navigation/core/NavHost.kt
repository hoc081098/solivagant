package com.hoc081098.solivagant.navigation.core

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.compose.SavedStateHandleFactoryProvider
import com.hoc081098.kmp.viewmodel.compose.ViewModelStoreOwnerProvider
import com.hoc081098.kmp.viewmodel.compose.kmpViewModel
import com.hoc081098.kmp.viewmodel.createSavedStateHandle
import com.hoc081098.kmp.viewmodel.viewModelFactory
import com.hoc081098.solivagant.navigation.core.internal.ExtraRoute
import com.hoc081098.solivagant.navigation.core.internal.NavEntry
import com.hoc081098.solivagant.navigation.core.internal.NavStoreViewModel
import com.hoc081098.solivagant.navigation.core.internal.rememberDefaultNavigator

public fun <T : Route> SavedStateHandle.requireRoute(): T {
  return requireNotNull(get<T>(ExtraRoute)) {
    "SavedStateHandle doesn't contain Route data in \"$ExtraRoute\""
  }
}

public val LocalNavigator: ProvidableCompositionLocal<Navigator> = staticCompositionLocalOf<Navigator> {
  error("Can't use Navigator outside of a navigator NavHost")
}

@Suppress("LongMethod")
@Composable
public fun NavHost(
  initialRoute: Route,
  @Suppress("UnstableCollections") contents: List<RouteContent<*>>,
  modifier: Modifier = Modifier,
) {
  val saveableStateHolder = rememberSaveableStateHolder()

  @Suppress("ViewModelInjection")
  val navStoreViewModel = kmpViewModel(
    factory = viewModelFactory {
      NavStoreViewModel(
        globalSavedStateHandle = createSavedStateHandle(),
      )
    },
  )

  val navigator = rememberDefaultNavigator(
    initialRoute = initialRoute,
    contents = contents,
    onStackEntryRemoved = remember(navStoreViewModel, saveableStateHolder) {
      {
          entry ->
        navStoreViewModel.removeEntry(entry.id)
        saveableStateHolder.removeState(entry.id)
        println("onStackEntryRemoved: ${entry.id}")
      }
    },
    navStoreViewModel = navStoreViewModel,
  )

  BackHandler(navigator)

  val transition = updateTransition(
    targetState = navigator.visibleEntryState.value,
    label = "NavHost#visibleEntry",
  )

  // Mark transition complete when the transition is finished
  LaunchedEffect(navigator, transition.currentState, transition.targetState) {
    if (transition.currentState == transition.targetState) {
      navigator.markTransitionComplete()
    }
  }

  CompositionLocalProvider(LocalNavigator provides navigator) {
    transition.AnimatedContent(
      modifier = modifier,
      transitionSpec = {
        val isPop = navigator.isPop.value

        val enter = if (isPop) {
          slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
          )
        } else {
          slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
          )
        }

        val exit = if (isPop) {
          slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
          )
        } else {
          slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
          )
        }
        (enter togetherWith exit).using(SizeTransform(clip = false))
      },
      contentKey = { it.id },
    ) { entry ->
      saveableStateHolder.SaveableStateProvider(key = entry.id) {
        NavEntryContent(
          entry,
          navStoreViewModel,
        )
      }
    }
  }
}

@Composable
private fun <T : Route> NavEntryContent(
  navEntry: NavEntry<T>,
  navStoreViewModel: NavStoreViewModel,
) {
  ViewModelStoreOwnerProvider(
    viewModelStoreOwner = navStoreViewModel provideViewModelStoreOwner navEntry.id,
  ) {
    SavedStateHandleFactoryProvider(
      savedStateHandleFactory = navStoreViewModel provideSavedStateHandleFactory navEntry,
    ) {
      navEntry.content.Content(route = navEntry.route)
    }
  }
}

@Composable
public expect fun BackHandler(
  navigator: Navigator = LocalNavigator.current,
)
