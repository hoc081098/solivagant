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

internal class AppLifecycleOwnerImpl : LifecycleOwner {
  private val lifecycleRegistry = LifecycleRegistry()
  override val lifecycle: Lifecycle get() = lifecycleRegistry

  // A notification that posts shortly before an app leaves the background state on its way to becoming the active app.
  private val willEnterForegroundObserver = addObserver(UIApplicationWillEnterForegroundNotification) {
    moveToStarted(lifecycleRegistry)
  }

  // A notification that posts when the app becomes active.
  private val didBecomeActiveObserver = addObserver(UIApplicationDidBecomeActiveNotification) {
    moveToResumed(lifecycleRegistry)
  }

  // A notification that posts when the app is no longer active and loses focus.
  private val willResignActiveObserver = addObserver(UIApplicationWillResignActiveNotification) {
    moveToPaused(lifecycleRegistry)
  }

  // A notification that posts when the app enters the background.
  private val didEnterBackgroundObserver = addObserver(UIApplicationDidEnterBackgroundNotification) {
    moveToStopped(lifecycleRegistry)
  }

  // Tells the delegate when the app is about to terminate.
  private val willTerminateObserver = addObserver(UIApplicationWillTerminateNotification) {
    moveToDestroyed(lifecycleRegistry)
  }

  init {
    NSOperationQueue.mainQueue.addOperationWithBlock {
      if (lifecycle.currentState == Lifecycle.State.INITIALIZED) {
        when (UIApplication.sharedApplication.applicationState) {
          UIApplicationState.UIApplicationStateActive -> {
            // The app is running in the foreground and currently receiving events.
            lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
            lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
          }

          UIApplicationState.UIApplicationStateInactive -> {
            // The app is running in the foreground but isn’t receiving events.
            lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
          }

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

private fun moveToStarted(lifecycleRegistry: LifecycleRegistry) {
  when (lifecycleRegistry.currentState) {
    Lifecycle.State.INITIALIZED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
    }

    Lifecycle.State.CREATED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
    }

    Lifecycle.State.STARTED -> {
      // Do nothing
    }

    Lifecycle.State.RESUMED, Lifecycle.State.DESTROYED -> {
      error("Cannot move to STARTED state from RESUMED nor DESTROYED state")
    }
  }
}

private fun moveToResumed(lifecycleRegistry: LifecycleRegistry) {
  when (lifecycleRegistry.currentState) {
    Lifecycle.State.INITIALIZED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
    }

    Lifecycle.State.CREATED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
    }

    Lifecycle.State.STARTED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_RESUME)
    }

    Lifecycle.State.RESUMED -> {
      // Do nothing
    }

    Lifecycle.State.DESTROYED -> {
      error("Cannot move to RESUMED state from DESTROYED state")
    }
  }
}

private fun moveToPaused(lifecycleRegistry: LifecycleRegistry) {
  when (lifecycleRegistry.currentState) {
    Lifecycle.State.INITIALIZED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
    }

    Lifecycle.State.CREATED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_START)
    }

    Lifecycle.State.STARTED -> {
      // Do nothing
    }

    Lifecycle.State.RESUMED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_PAUSE)
    }

    Lifecycle.State.DESTROYED -> {
      error("Cannot move to PAUSED state from DESTROYED state")
    }
  }
}

private fun moveToStopped(lifecycleRegistry: LifecycleRegistry) {
  when (lifecycleRegistry.currentState) {
    Lifecycle.State.INITIALIZED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
    }

    Lifecycle.State.CREATED -> {
      // Do nothing
    }

    Lifecycle.State.STARTED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
    }

    Lifecycle.State.RESUMED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_PAUSE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
    }

    Lifecycle.State.DESTROYED -> {
      error("Cannot move to STOPPED state from DESTROYED state")
    }
  }
}

private fun moveToDestroyed(lifecycleRegistry: LifecycleRegistry) {
  when (lifecycleRegistry.currentState) {
    Lifecycle.State.INITIALIZED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_CREATE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }

    Lifecycle.State.CREATED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }

    Lifecycle.State.STARTED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }

    Lifecycle.State.RESUMED -> {
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_PAUSE)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_STOP)
      lifecycleRegistry.onStateChanged(Lifecycle.Event.ON_DESTROY)
    }

    Lifecycle.State.DESTROYED ->
      Unit
  }
}

@Composable
internal actual fun rememberPlatformLifecycleOwner(): LifecycleOwner {
  return remember { AppLifecycleOwnerImpl() }
}
