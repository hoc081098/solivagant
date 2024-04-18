package com.hoc081098.solivagant.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LifecycleOwnerProvider
import com.hoc081098.solivagant.navigation.LifecycleOwnerComposeUIViewControllerDelegate
import com.hoc081098.solivagant.navigation.ProvideCompositionLocals
import com.hoc081098.solivagant.navigation.SavedStateSupport
import com.hoc081098.solivagant.navigation.bindTo
import com.hoc081098.solivagant.sample.common.OnLifecycleEventWithBuilder
import io.github.aakira.napier.Napier
import org.koin.core.component.get
import platform.UIKit.UIViewController

private val LifecycleObserver by lazy {
  Lifecycle.Observer { Napier.d(message = "[MainViewController] [outer] Lifecycle event: $it", tag = "[main]") }
}

@Suppress("FunctionName", "unused") // Called from platform code
fun MainViewController(savedStateSupport: SavedStateSupport): UIViewController {
  val lifecycleOwnerUIVcDelegate =
    LifecycleOwnerComposeUIViewControllerDelegate(hostLifecycleOwner = DIContainer.get())
      .apply { bindTo(savedStateSupport) }
      .apply { lifecycle.subscribe(LifecycleObserver) }

  Napier.d(
    message = "[MainViewController] savedStateSupport=$savedStateSupport, lifecycleOwner=$lifecycleOwnerUIVcDelegate",
    tag = "[main]",
  )

  return ComposeUIViewController(
    configure = { delegate = lifecycleOwnerUIVcDelegate },
  ) {
    DebugLog()

    LifecycleOwnerProvider(lifecycleOwnerUIVcDelegate) {
      OnLifecycleEventWithBuilder {
        onEach { Napier.d(message = "[MainViewController] [inner] Lifecycle event: $it", tag = "[main]") }
      }

      savedStateSupport.ProvideCompositionLocals { SolivagantSampleApp() }
    }
  }
}

/**
 * Debug log for recomposition, remember, forgotten, abandoned.
 */
@Composable
private inline fun DebugLog() {
  remember {
    Napier.d(message = "[MainViewController] enter composition", tag = "[main]")

    object : RememberObserver {
      override fun onForgotten() = Napier.d(message = "MainViewController forgotten", tag = "[main]")

      override fun onRemembered() = Unit

      override fun onAbandoned() = Unit
    }
  }

  SideEffect { Napier.d(message = "[MainViewController] recomposition...", tag = "[main]") }
}
