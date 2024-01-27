package com.hoc081098.solivagant.sample

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.navigation.internal.AppLifecycleOwner
import com.hoc081098.solivagant.sample.common.OnLifecycleEventWithBuilder
import io.github.aakira.napier.Napier
import platform.UIKit.UIViewController

internal val AppLifecycleOwner by lazy { AppLifecycleOwner() }

@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController = ComposeUIViewController {
  CompositionLocalProvider(LocalLifecycleOwner provides AppLifecycleOwner) {
    OnLifecycleEventWithBuilder {
      onEach { Napier.d(message = "Lifecycle event: $it", tag = "[main]") }
    }

    SolivagantSampleApp()
  }
}
