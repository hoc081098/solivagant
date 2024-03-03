package com.hoc081098.solivagant.sample.wasm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  println(">>> main")
  CanvasBasedWindow(canvasElementId = "ComposeTarget") { App() }
}
