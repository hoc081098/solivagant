package com.hoc081098.solivagant.sample

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hoc081098.solivagant.lifecycle.LifecycleOwnerProvider
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import com.hoc081098.solivagant.lifecycle.rememberLifecycleOwner
import com.hoc081098.solivagant.navigation.internal.LifecycleControllerEffect
import com.hoc081098.solivagant.sample.common.OnLifecycleEventWithBuilder
import io.github.aakira.napier.Napier

fun main() {
  startKoinCommon()
  setupNapier()

  application {
    val windowState = rememberWindowState()
    val lifecycleRegistry = remember { LifecycleRegistry() }
    val lifecycleOwner = rememberLifecycleOwner(lifecycleRegistry)

    LifecycleControllerEffect(
      lifecycleRegistry = lifecycleRegistry,
      windowState = windowState,
    )

    Window(
      onCloseRequest = ::exitApplication,
      title = "Solivagant sample",
      state = windowState,
    ) {
      LifecycleOwnerProvider(lifecycleOwner) {
        OnLifecycleEventWithBuilder {
          onEach { Napier.d(message = "Lifecycle event: $it", tag = "[main]") }
        }

        SolivagantSampleApp()
      }
    }
  }
}

@Preview
@Composable
private fun AppDesktopPreview() {
  SolivagantSampleApp()
}
