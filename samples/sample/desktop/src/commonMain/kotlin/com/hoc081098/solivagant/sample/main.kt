package com.hoc081098.solivagant.sample

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hoc081098.kmp.viewmodel.compose.LocalSavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.compose.LocalViewModelStoreOwner
import com.hoc081098.solivagant.lifecycle.LifecycleOwnerProvider
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import com.hoc081098.solivagant.lifecycle.rememberLifecycleOwner
import com.hoc081098.solivagant.navigation.LifecycleControllerEffect
import com.hoc081098.solivagant.navigation.SavedStateSupport
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

    val savedStateSupport = remember { SavedStateSupport() }
    DisposableEffect(Unit) {
      onDispose(savedStateSupport::clear)
    }

    Window(
      onCloseRequest = ::exitApplication,
      title = "Solivagant sample",
      state = windowState,
    ) {
      LifecycleOwnerProvider(lifecycleOwner) {
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
}

@Preview
@Composable
private fun AppDesktopPreview() {
  SolivagantSampleApp()
}
