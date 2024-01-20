package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.hoc081098.kmp.viewmodel.parcelable.Parcelable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.repeatOnLifecycle
import com.hoc081098.solivagant.navigation.internal.InternalNavigationApi
import com.hoc081098.solivagant.navigation.internal.NavEvent
import com.hoc081098.solivagant.navigation.internal.NavigationExecutor
import com.hoc081098.solivagant.navigation.internal.VisibleForTesting
import com.hoc081098.solivagant.navigation.internal.currentBackPressedDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Sets up the [NavEventNavigator] inside the current composition so that it's events
 * are handled while the composition is active.
 */
@Composable
public fun NavigationSetup(navigator: NavEventNavigator) {
  val executor = LocalNavigationExecutor.current

  navigator.navigationResultRequests.forEach {
    LaunchedEffect(executor, it) {
      executor.collectAndHandleNavigationResults(it)
    }
  }

  val backDispatcher = currentBackPressedDispatcher()
  DisposableEffect(backDispatcher, navigator) {
    backDispatcher.addCallback(navigator.onBackPressedCallback)

    onDispose {
      navigator.onBackPressedCallback.remove()
    }
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  LaunchedEffect(lifecycleOwner, executor, navigator) {
    navigator.collectAndHandleNavEvents(lifecycleOwner, executor)
  }
}

@VisibleForTesting
internal suspend fun NavEventNavigator.collectAndHandleNavEvents(
  lifecycleOwner: LifecycleOwner,
  executor: NavigationExecutor,
) {
  // Following comment https://github.com/Kotlin/kotlinx.coroutines/issues/2886#issuecomment-901188295,
  // the events could be lost due to the prompt cancellation guarantee of Channel,
  // we must use `Dispatchers.Main.immediate` to receive events.
  //
  // Note, when calling this method from a Composable,
  // the dispatcher of the Compose Side-effect is [androidx.compose.ui.platform.AndroidUiDispatcher],
  // it does not execute coroutines immediately when the current thread is the main thread,
  // but performs the dispatch during a handler callback or choreographer animation frame stage,
  // whichever comes first. Basically, it has some certain delay compared to [Dispatchers.Main.immediate].
  // So we must switch to [Dispatchers.Main.immediate] before collecting events.
  withContext(Dispatchers.Main.immediate) {
    lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
      navEvents.collect { event ->
        executor.navigateTo(event)
      }
    }
  }
}

private fun NavigationExecutor.navigateTo(
  event: NavEvent,
) {
  when (event) {
    is NavEvent.NavigateToEvent -> {
      navigateTo(event.route)
    }

    is NavEvent.NavigateToRootEvent -> {
      navigateToRoot(event.root, event.restoreRootState)
    }

    is NavEvent.UpEvent -> {
      navigateUp()
    }

    is NavEvent.BackEvent -> {
      navigateBack()
    }

    is NavEvent.BackToEvent -> {
      navigateBackToInternal(event.popUpTo, event.inclusive)
    }

    is NavEvent.ResetToRoot -> {
      resetToRoot(event.root)
    }

    is NavEvent.ReplaceAll -> {
      replaceAll(event.root)
    }

    is NavEvent.DestinationResultEvent<*> -> {
      savedStateHandleFor(event.key.destinationId)[event.key.requestKey] = event.result
    }

    is NavEvent.MultiNavEvent -> {
      event.navEvents.forEach { navigateTo(it) }
    }
  }
}

@VisibleForTesting
internal suspend fun <R : Parcelable> NavigationExecutor.collectAndHandleNavigationResults(
  request: NavigationResultRequest<R>,
) {
  val savedStateHandle = savedStateHandleFor(request.key.destinationId)
  savedStateHandle.getStateFlow<Parcelable>(request.key.requestKey, InitialValue)
    .collect {
      if (it != InitialValue) {
        @Suppress("UNCHECKED_CAST")
        request.onResult(it as R)
        savedStateHandle[request.key.requestKey] = InitialValue
      }
    }
}

@Parcelize
private object InitialValue : Parcelable

@InternalNavigationApi
public val LocalNavigationExecutor: ProvidableCompositionLocal<NavigationExecutor> = staticCompositionLocalOf {
  throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
