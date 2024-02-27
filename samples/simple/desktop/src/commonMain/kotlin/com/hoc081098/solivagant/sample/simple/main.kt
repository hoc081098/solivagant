package com.hoc081098.solivagant.sample.simple

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.lifecycle.compose.currentStateAsState
import com.hoc081098.solivagant.lifecycle.compose.rememberLifecycleOwner
import com.hoc081098.solivagant.navigation.LifecycleControllerEffect
import com.hoc081098.solivagant.navigation.LocalProvider
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

    DisposableEffect(Unit) {
      onDispose(savedStateSupport::clear)
    }

    // if (produceState<Boolean>(true) { while (true) { delay(7000); value=!value }  }.value)
    Window(
      onCloseRequest = ::exitApplication,
      title = "Simple Solivagant sample $lifecycleState",
      state = windowState,
    ) {
      savedStateSupport.LocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        SimpleSolivagantSampleApp()

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

@Preview
@Composable
private fun AppDesktopPreview() {
  SimpleSolivagantSampleApp()
}
