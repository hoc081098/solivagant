package com.hoc081098.solivagant.sample

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.navigation.internal.LifecycleControllerEffect
import com.hoc081098.solivagant.sample.common.OnLifecycleEventWithBuilder
import io.github.aakira.napier.Napier

fun main() {
  startKoinCommon()
  setupNapier()

  application {
    val windowState = rememberWindowState()
    val lifecycle = remember { LifecycleRegistry() }
    val lifecycleOwner = remember(lifecycle) {
      object : LifecycleOwner {
        override val lifecycle get() = lifecycle
      }
    }

    LifecycleControllerEffect(
      lifecycleRegistry = lifecycle,
      windowState = windowState,
    )

    Window(
      onCloseRequest = ::exitApplication,
      title = "Solivagant Sample",
      state = windowState,
    ) {
      CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
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
fun AppDesktopPreview() {
  SolivagantSampleApp()
}
