package com.hoc081098.solivagant.sample.simple

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hoc081098.kmp.viewmodel.compose.LocalSavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.compose.LocalViewModelStoreOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.lifecycle.compose.currentStateAsState
import com.hoc081098.solivagant.lifecycle.rememberLifecycleOwner
import com.hoc081098.solivagant.navigation.LifecycleControllerEffect
import com.hoc081098.solivagant.navigation.SavedStateSupport

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
    val lifecycleState by lifecycleRegistry.currentStateAsState()

    val savedStateSupport = remember { SavedStateSupport() }
    DisposableEffect(Unit) {
      onDispose(savedStateSupport::clear)
    }

    Window(
      onCloseRequest = ::exitApplication,
      title = "Simple Solivagant sample $lifecycleState",
      state = windowState,
    ) {
      CompositionLocalProvider(
        LocalViewModelStoreOwner provides savedStateSupport,
        LocalSaveableStateRegistry provides savedStateSupport,
        LocalSavedStateHandleFactory provides savedStateSupport,
        LocalLifecycleOwner provides lifecycleOwner,
      ) {
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
