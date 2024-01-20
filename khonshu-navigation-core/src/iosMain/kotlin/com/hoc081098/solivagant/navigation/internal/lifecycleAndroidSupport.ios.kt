package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.interop.LocalUIViewController
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIApplicationWillTerminateNotification
import platform.UIKit.UIViewController
import platform.darwin.NSObjectProtocol

internal actual interface LifecycleOwner

private class LifecycleOwnerImpl(
  private val uiViewController: UIViewController,
) : LifecycleOwner {

  private val willEnterForegroundObserver = addObserver(UIApplicationWillEnterForegroundNotification) { lifecycle.start() }
  private val didBecomeActiveObserver = addObserver(UIApplicationDidBecomeActiveNotification) { lifecycle.resume() }
  private val willResignActiveObserver = addObserver(UIApplicationWillResignActiveNotification) { lifecycle.pause() }
  private val didEnterBackgroundObserver = addObserver(UIApplicationDidEnterBackgroundNotification) { lifecycle.stop() }
  private val willTerminateObserver = addObserver(UIApplicationWillTerminateNotification) { lifecycle.destroy() }

  init {
  }
}

@Composable
@ReadOnlyComposable
internal actual fun currentLifecycleOwner(): LifecycleOwner = LifecycleOwnerImpl(
  uiViewController = LocalUIViewController.current,
)

internal actual suspend fun LifecycleOwner.repeatOnResumeLifecycle(block: suspend () -> Unit) {
  UIApplication.sharedApplication().delegate
}

private fun addObserver(name: NSNotificationName, block: (NSNotification?) -> Unit): NSObjectProtocol =
  NSNotificationCenter.defaultCenter.addObserverForName(
    name = name,
    `object` = null,
    queue = NSOperationQueue.mainQueue,
    usingBlock = block,
  )
