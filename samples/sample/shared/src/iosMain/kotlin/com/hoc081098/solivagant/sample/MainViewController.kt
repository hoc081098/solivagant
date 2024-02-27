package com.hoc081098.solivagant.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.window.ComposeUIViewController
import com.hoc081098.kmp.viewmodel.compose.LocalSavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.compose.LocalViewModelStoreOwner
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleOwnerProvider
import com.hoc081098.solivagant.navigation.LifecycleOwnerComposeUIViewControllerDelegate
import com.hoc081098.solivagant.navigation.SavedStateSupport
import com.hoc081098.solivagant.sample.common.OnLifecycleEventWithBuilder
import io.github.aakira.napier.Napier
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused") // Called from platform code
fun MainViewController(savedStateSupport: SavedStateSupport): UIViewController {
  val lifecycleOwnerUIVcDelegate = LifecycleOwnerComposeUIViewControllerDelegate(
    hostLifecycleOwner = DIContainer
      .getKoin()
      .get<LifecycleOwner>(),
  )

  // When [SavedStateSupport.clear] is called,
  // we move the lifecycle to [Lifecycle.State.DESTROYED].
  savedStateSupport.addCloseable(key = lifecycleOwnerUIVcDelegate, closeOldCloseable = false) {
    lifecycleOwnerUIVcDelegate.onDestroy()
  }

  Napier.d(
    message = "MainViewController savedStateSupport=$savedStateSupport, lifecycleOwner=$lifecycleOwnerUIVcDelegate",
    tag = "[main]",
  )

  return ComposeUIViewController(
    configure = { delegate = lifecycleOwnerUIVcDelegate },
  ) {
    DebugLog()

    LifecycleOwnerProvider(lifecycleOwnerUIVcDelegate) {
      OnLifecycleEventWithBuilder {
        onEach { Napier.d(message = "Lifecycle event: $it", tag = "[main]") }
      }

      CompositionLocalProvider(
        LocalViewModelStoreOwner provides savedStateSupport,
        LocalSaveableStateRegistry provides savedStateSupport,
        LocalSavedStateHandleFactory provides savedStateSupport,
      ) {
        SolivagantSampleApp()

        // Must be at the last,
        // because onDispose is called in reverse order, so we want to save state first,
        // before [SaveableStateRegistry.Entry]s are unregistered.
        DisposableEffect(Unit) {
          onDispose(savedStateSupport::performSave)
        }
      }
    }
  }
}

/**
 * Debug log for recomposition, remember, forgotten, abandoned.
 */
@Composable
private inline fun DebugLog() {
  SideEffect { Napier.d(message = "MainViewController recomposition", tag = "[main]") }

  remember {
    Napier.d(message = "MainViewController enter composition", tag = "[main]")
    object : RememberObserver {
      override fun onAbandoned() {
        Napier.d(message = "MainViewController abandoned", tag = "[main]")
      }

      override fun onForgotten() {
        Napier.d(message = "MainViewController forgotten", tag = "[main]")
      }

      override fun onRemembered() = Unit
    }
  }
}
