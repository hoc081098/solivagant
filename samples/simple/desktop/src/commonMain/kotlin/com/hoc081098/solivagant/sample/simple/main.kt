package com.hoc081098.solivagant.sample.simple

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.lifecycle.compose.currentStateAsState
import com.hoc081098.solivagant.lifecycle.compose.rememberLifecycleOwner
import com.hoc081098.solivagant.navigation.ClearOnDispose
import com.hoc081098.solivagant.navigation.LifecycleControllerEffect
import com.hoc081098.solivagant.navigation.ProvideCompositionLocals
import com.hoc081098.solivagant.navigation.SavedStateSupport
import org.koin.core.logger.Level

fun main() {
  startKoinCommon {
    printLogger(level = Level.DEBUG)
  }
  setupNapier()

  val lifecycleRegistry = LifecycleRegistry()
  val savedStateSupport = SavedStateSupport()

  application {
    val windowState = rememberWindowState()
    val lifecycleOwner = rememberLifecycleOwner(lifecycleRegistry)

    LifecycleControllerEffect(
      lifecycleRegistry = lifecycleRegistry,
      windowState = windowState,
    )
    val lifecycleState by lifecycleRegistry.currentStateAsState()

    savedStateSupport.ClearOnDispose()

    // if (produceState<Boolean>(true) { while (true) { delay(7000); value=!value }  }.value)
    Window(
      onCloseRequest = ::exitApplication,
      title = "Simple Solivagant sample $lifecycleState",
      state = windowState,
    ) {
      savedStateSupport.ProvideCompositionLocals(LocalLifecycleOwner provides lifecycleOwner) {
        SimpleSolivagantSampleApp()
      }
    }
  }
}

@Preview
@Composable
private fun AppDesktopPreview() {
  SimpleSolivagantSampleApp()
}
