package com.hoc081098.solivagant.sample

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.window.ComposeUIViewController
import com.hoc081098.kmp.viewmodel.compose.LocalSavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.compose.LocalViewModelStoreOwner
import com.hoc081098.solivagant.lifecycle.LifecycleOwnerProvider
import com.hoc081098.solivagant.navigation.AppLifecycleOwner
import com.hoc081098.solivagant.navigation.LifecycleOwnerComposeUIViewControllerDelegate
import com.hoc081098.solivagant.navigation.SavedStateSupport
import com.hoc081098.solivagant.sample.common.OnLifecycleEventWithBuilder
import io.github.aakira.napier.Napier
import platform.UIKit.UIViewController

internal val AppLifecycleOwner by lazy { AppLifecycleOwner() }

@Suppress("FunctionName", "unused")
fun MainViewController(savedStateSupport: SavedStateSupport?): UIViewController {
  val lifecycleOwner = LifecycleOwnerComposeUIViewControllerDelegate(AppLifecycleOwner)

  return ComposeUIViewController(configure = { delegate = lifecycleOwner }) {
    LifecycleOwnerProvider(lifecycleOwner) {
      OnLifecycleEventWithBuilder {
        onEach { Napier.d(message = "Lifecycle event: $it", tag = "[main]") }
      }

      if (savedStateSupport != null) {
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
      } else {
        SolivagantSampleApp()
      }
    }
  }
}
