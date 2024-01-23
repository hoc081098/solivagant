package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hoc081098.solivagant.lifecycle.Lifecycle

import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIApplicationWillTerminateNotification
import platform.darwin.NSObjectProtocol

private class AppLifecycleOwner : LifecycleOwner {
  private val lifecycleRegistry = LifecycleRegistry()
  override val lifecycle: Lifecycle get() = lifecycleRegistry

  // A notification that posts shortly before an app leaves the background state on its way to becoming the active app.
  private val willEnterForegroundObserver = addObserver(UIApplicationWillEnterForegroundNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
  }

  // A notification that posts when the app becomes active.
  private val didBecomeActiveObserver = addObserver(UIApplicationDidBecomeActiveNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
  }

  // A notification that posts when the app is no longer active and loses focus.
  private val willResignActiveObserver = addObserver(UIApplicationWillResignActiveNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_PAUSE)
  }

  // A notification that posts when the app enters the background.
  private val didEnterBackgroundObserver = addObserver(UIApplicationDidEnterBackgroundNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
  }

  // Tells the delegate when the app is about to terminate.
  private val willTerminateObserver = addObserver(UIApplicationWillTerminateNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
  }

  init {
    NSOperationQueue.mainQueue.addOperationWithBlock {
      if (lifecycle.currentState == Lifecycle.State.INITIALIZED) {
        when (UIApplication.sharedApplication.applicationState) {
          UIApplicationState.UIApplicationStateActive ->
            // The app is running in the foreground and currently receiving events.
            lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)

          UIApplicationState.UIApplicationStateInactive ->
            // The app is running in the foreground but isn’t receiving events.
            lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)

          UIApplicationState.UIApplicationStateBackground ->
            // The app is running in the background.
            lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)

          else -> lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
        }
      }
    }

    lifecycleRegistry.subscribe { event ->
      if (event == Lifecycle.Event.ON_DESTROY) {
        removeObserver(willEnterForegroundObserver)
        removeObserver(didBecomeActiveObserver)
        removeObserver(willResignActiveObserver)
        removeObserver(didEnterBackgroundObserver)
        removeObserver(willTerminateObserver)
      }
    }
  }
}

private fun addObserver(name: NSNotificationName, block: (NSNotification?) -> Unit): NSObjectProtocol =
  NSNotificationCenter.defaultCenter.addObserverForName(
    name = name,
    `object` = null,
    queue = NSOperationQueue.mainQueue,
    usingBlock = block,
  )

private fun removeObserver(observer: NSObjectProtocol) =
  NSNotificationCenter.defaultCenter.removeObserver(observer)

@Composable
internal actual fun rememberPlatformLifecycleOwner(): LifecycleOwner {
  return remember { AppLifecycleOwner() }
}
