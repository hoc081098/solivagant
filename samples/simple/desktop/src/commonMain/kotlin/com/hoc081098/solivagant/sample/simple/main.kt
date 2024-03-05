package com.hoc081098.solivagant.sample.simple

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.navigation.ClearOnDispose
import com.hoc081098.solivagant.navigation.ExperimentalSolivagantApi
import com.hoc081098.solivagant.navigation.ProvideCompositionLocals
import com.hoc081098.solivagant.navigation.SavedStateSupport
import com.hoc081098.solivagant.navigation.rememberWindowLifecycleOwner
import kotlinx.coroutines.delay
import org.koin.core.logger.Level

@OptIn(ExperimentalSolivagantApi::class)
fun main() {
  startKoinCommon {
    printLogger(level = Level.DEBUG)
  }
  setupNapier()

  val savedStateSupport = SavedStateSupport()

  application {
    savedStateSupport.ClearOnDispose()

    // if (rememberWindowVisibilityState().value)
    Window(
      onCloseRequest = ::exitApplication,
      title = "Simple Solivagant sample",
    ) {
      val lifecycleOwner = checkNotNull(rememberWindowLifecycleOwner()) {
        "rememberWindowLifecycleOwner returns null"
      }

      savedStateSupport.ProvideCompositionLocals(LocalLifecycleOwner provides lifecycleOwner) {
        SimpleSolivagantSampleApp()
      }
    }
  }
}

@Suppress("UnusedPrivateMember", "unused") // To demo
@Composable
private fun rememberWindowVisibilityState(): State<Boolean> =
  produceState(true) {
    while (true) {
      delay(7000)
      value = !value
    }
  }

@Preview
@Composable
private fun AppDesktopPreview() {
  SimpleSolivagantSampleApp()
}
