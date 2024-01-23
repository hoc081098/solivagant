package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hoc081098.solivagant.lifecycle.Lifecycle

import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.LifecycleRegistry
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationDidBecomeActiveNotification
import platform.AppKit.NSApplicationDidHideNotification
import platform.AppKit.NSApplicationWillResignActiveNotification
import platform.AppKit.NSApplicationWillTerminateNotification
import platform.AppKit.NSApplicationWillUnhideNotification
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName
import platform.Foundation.NSOperationQueue
import platform.darwin.NSObjectProtocol

private class AppLifecycleOwner : LifecycleOwner {
  private val lifecycleRegistry = LifecycleRegistry()
  override val lifecycle: Lifecycle get() = lifecycleRegistry

  private val willUnHideObserver = addObserver(NSApplicationWillUnhideNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
  }

  private val didBecomeActiveObserver = addObserver(NSApplicationDidBecomeActiveNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
  }

  private val willResignActiveObserver = addObserver(NSApplicationWillResignActiveNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_PAUSE)
  }

  private val didHideObserver = addObserver(NSApplicationDidHideNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
  }

  private val willTerminateObserver = addObserver(NSApplicationWillTerminateNotification) {
    lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
  }

  init {
    NSOperationQueue.mainQueue.addOperationWithBlock {
      if (lifecycle.currentState == Lifecycle.State.INITIALIZED) {
        if (NSApplication.sharedApplication.active) {
          lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
        } else {
          lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
        }
      }
    }

    lifecycleRegistry.subscribe { event ->
      if (event == Lifecycle.Event.ON_DESTROY) {
        removeObserver(willUnHideObserver)
        removeObserver(didBecomeActiveObserver)
        removeObserver(willResignActiveObserver)
        removeObserver(didHideObserver)
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
