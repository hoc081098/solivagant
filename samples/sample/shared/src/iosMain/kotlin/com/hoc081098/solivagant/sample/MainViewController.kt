package com.hoc081098.solivagant.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionName")
fun MainViewController(): UIViewController = ComposeUIViewController { SolivagantSampleApp() }
