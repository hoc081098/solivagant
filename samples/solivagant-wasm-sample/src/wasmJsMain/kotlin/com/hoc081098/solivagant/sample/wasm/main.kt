package com.hoc081098.solivagant.sample.wasm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.hoc081098.solivagant.lifecycle.LenientLifecycleRegistry
import com.hoc081098.solivagant.lifecycle.Lifecycle
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.lifecycle.compose.rememberLifecycleOwner
import com.hoc081098.solivagant.navigation.ClearOnDispose
import com.hoc081098.solivagant.navigation.ProvideCompositionLocals
import com.hoc081098.solivagant.navigation.SavedStateSupport
import kotlin.random.Random
import kotlinx.browser.document
import org.w3c.dom.get

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  println(">>> [main] ${getJsDateISOString()}")

  val lifecycleRegistry = LenientLifecycleRegistry(Lifecycle.State.RESUMED)
    .apply { attachToDocument() }
    .apply {
      subscribe { event ->
        println(">>> [main] ${getJsDateISOString()} lifecycle=$event")
      }
    }

  val savedStateSupport = SavedStateSupport()
    .apply {
      addCloseable(Random.nextInt()) {
        println(">>> [main] ${getJsDateISOString()} $this::closed")
      }
    }

  CanvasBasedWindow(
    title = "Solivagant Wasm Sample",
    canvasElementId = "ComposeTarget",
  ) {
    savedStateSupport.ClearOnDispose()

    savedStateSupport.ProvideCompositionLocals(
      LocalLifecycleOwner provides rememberLifecycleOwner(lifecycleRegistry),
    ) {
      App()
    }
  }
}

private fun getJsDateISOString(): JsString = js("new Date().toISOString()")

private fun LenientLifecycleRegistry.attachToDocument() {
  fun onVisibilityChanged() {
    val isVisible = document["visibilityState"] == "visible".toJsString()

    if (isVisible) {
      onStateChanged(Lifecycle.Event.ON_RESUME)
    } else {
      onStateChanged(Lifecycle.Event.ON_STOP)
    }
  }
  document.addEventListener("visibilitychange", callback = { onVisibilityChanged() })
}
