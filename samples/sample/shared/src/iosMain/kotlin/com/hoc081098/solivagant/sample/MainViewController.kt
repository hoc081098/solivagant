package com.hoc081098.solivagant.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.hoc081098.solivagant.lifecycle.LifecycleOwnerProvider
import com.hoc081098.solivagant.navigation.LifecycleOwnerComposeUIViewControllerDelegate
import com.hoc081098.solivagant.navigation.LocalProvider
import com.hoc081098.solivagant.navigation.SavedStateSupport
import com.hoc081098.solivagant.sample.common.OnLifecycleEventWithBuilder
import io.github.aakira.napier.Napier
import org.koin.core.component.get
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused") // Called from platform code
fun MainViewController(savedStateSupport: SavedStateSupport): UIViewController {
  val lifecycleOwnerUIVcDelegate = LifecycleOwnerComposeUIViewControllerDelegate(
    hostLifecycleOwner = DIContainer.get(),
  )

  // When [SavedStateSupport.clear] is called,
  // we move the lifecycle to [Lifecycle.State.DESTROYED].
  savedStateSupport.addCloseable(key = lifecycleOwnerUIVcDelegate, closeOldCloseable = false) {
    Napier.d(message = "destroy $lifecycleOwnerUIVcDelegate", tag = "[main]")
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

      savedStateSupport.LocalProvider {
        SolivagantSampleApp()
      }
    }
  }
}

/**
 * Debug log for recomposition, remember, forgotten, abandoned.
 */
@Composable
private inline fun DebugLog() {
  remember {
    Napier.d(message = "MainViewController enter composition", tag = "[main]")

    object : RememberObserver {
      override fun onAbandoned() = Napier.d(message = "MainViewController abandoned", tag = "[main]")

      override fun onForgotten() = Napier.d(message = "MainViewController forgotten", tag = "[main]")

      override fun onRemembered() = Unit
    }
  }

  SideEffect { Napier.d(message = "MainViewController recomposition", tag = "[main]") }
}
