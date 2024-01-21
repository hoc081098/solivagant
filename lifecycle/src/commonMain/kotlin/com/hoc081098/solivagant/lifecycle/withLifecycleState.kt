package com.hoc081098.solivagant.lifecycle

import com.hoc081098.solivagant.lifecycle.Lifecycle.Companion.currentState
import com.hoc081098.solivagant.lifecycle.internal.AtomicReference
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * A [CancellationException] that indicates that the [Lifecycle] associated with an operation
 * reached the [Lifecycle.State.DESTROYED] state before the operation could complete.
 */
public class LifecycleDestroyedException : CancellationException(null)

/**
 * Run [block] with this [Lifecycle] in a [Lifecycle.State] of at least [state] and
 * resume with the result. Throws the [CancellationException] [LifecycleDestroyedException]
 * if the lifecycle has reached [Lifecycle.State.DESTROYED] by the time of the call or before
 * [block] is able to run.
 */
public suspend inline fun <R> Lifecycle.withStateAtLeast(
  state: Lifecycle.State,
  crossinline block: () -> R,
): R {
  require(state >= Lifecycle.State.CREATED) {
    "target state must be CREATED or greater, found $state"
  }

  return withStateAtLeastUnchecked(state, block)
}

/**
 * Run [block] with this [Lifecycle] in a [Lifecycle.State] of at least [Lifecycle.State.CREATED]
 * and resume with the result. Throws the [CancellationException] [LifecycleDestroyedException]
 * if the lifecycle has reached [Lifecycle.State.DESTROYED] by the time of the call or before
 * [block] is able to run.
 */
public suspend inline fun <R> Lifecycle.withCreated(
  crossinline block: () -> R,
): R = withStateAtLeastUnchecked(
  state = Lifecycle.State.CREATED,
  block = block,
)

/**
 * Run [block] with this [Lifecycle] in a [Lifecycle.State] of at least [Lifecycle.State.STARTED]
 * and resume with the result. Throws the [CancellationException] [LifecycleDestroyedException]
 * if the lifecycle has reached [Lifecycle.State.DESTROYED] by the time of the call or before
 * [block] is able to run.
 */
public suspend inline fun <R> Lifecycle.withStarted(
  crossinline block: () -> R,
): R = withStateAtLeastUnchecked(
  state = Lifecycle.State.STARTED,
  block = block,
)

/**
 * Run [block] with this [Lifecycle] in a [Lifecycle.State] of at least [Lifecycle.State.RESUMED]
 * and resume with the result. Throws the [CancellationException] [LifecycleDestroyedException]
 * if the lifecycle has reached [Lifecycle.State.DESTROYED] by the time of the call or before
 * [block] is able to run.
 */
public suspend inline fun <R> Lifecycle.withResumed(
  crossinline block: () -> R,
): R = withStateAtLeastUnchecked(
  state = Lifecycle.State.RESUMED,
  block = block,
)

/**
 * Run [block] with this [LifecycleOwner]'s [Lifecycle] in a [Lifecycle.State] of at least [state]
 * and resume with the result. Throws the [CancellationException] [LifecycleDestroyedException]
 * if the lifecycle has reached [Lifecycle.State.DESTROYED] by the time of the call or before
 * [block] is able to run.
 */
public suspend inline fun <R> LifecycleOwner.withStateAtLeast(
  state: Lifecycle.State,
  crossinline block: () -> R,
): R = lifecycle.withStateAtLeast(
  state = state,
  block = block,
)

/**
 * Run [block] with this [LifecycleOwner]'s [Lifecycle] in a [Lifecycle.State] of at least
 * [Lifecycle.State.CREATED] and resume with the result.
 * Throws the [CancellationException] [LifecycleDestroyedException] if the lifecycle has reached
 * [Lifecycle.State.DESTROYED] by the time of the call or before [block] is able to run.
 */
public suspend inline fun <R> LifecycleOwner.withCreated(
  crossinline block: () -> R,
): R = lifecycle.withStateAtLeastUnchecked(
  state = Lifecycle.State.CREATED,
  block = block,
)

/**
 * Run [block] with this [LifecycleOwner]'s [Lifecycle] in a [Lifecycle.State] of at least
 * [Lifecycle.State.STARTED] and resume with the result.
 * Throws the [CancellationException] [LifecycleDestroyedException] if the lifecycle has reached
 * [Lifecycle.State.DESTROYED] by the time of the call or before [block] is able to run.
 */
public suspend inline fun <R> LifecycleOwner.withStarted(
  crossinline block: () -> R,
): R = lifecycle.withStateAtLeastUnchecked(
  state = Lifecycle.State.STARTED,
  block = block,
)

/**
 * Run [block] with this [LifecycleOwner]'s [Lifecycle] in a [Lifecycle.State] of at least
 * [Lifecycle.State.RESUMED] and resume with the result.
 * Throws the [CancellationException] [LifecycleDestroyedException] if the lifecycle has reached
 * [Lifecycle.State.DESTROYED] by the time of the call or before [block] is able to run.
 */
public suspend inline fun <R> LifecycleOwner.withResumed(
  crossinline block: () -> R,
): R = lifecycle.withStateAtLeastUnchecked(
  state = Lifecycle.State.RESUMED,
  block = block,
)

/**
 * The inlined check for whether dispatch is necessary to perform [Lifecycle.withStateAtLeast]
 * operations that does not bounds-check [state]. Used internally when we know the target state
 * is already in bounds. Runs [block] inline without allocating if possible.
 */
@PublishedApi
internal suspend inline fun <R> Lifecycle.withStateAtLeastUnchecked(
  state: Lifecycle.State,
  crossinline block: () -> R,
): R {
  // Fast path: if our lifecycle dispatcher doesn't require dispatch we can check
  // the current lifecycle state and decide if we can run synchronously
  val lifecycleDispatcher = Dispatchers.Main.immediate
  val dispatchNeeded = lifecycleDispatcher.isDispatchNeeded(coroutineContext)
  if (!dispatchNeeded) {
    if (currentState == Lifecycle.State.DESTROYED) throw LifecycleDestroyedException()
    if (currentState >= state) return block()
  }

  return suspendWithStateAtLeastUnchecked(state, dispatchNeeded, lifecycleDispatcher) {
    block()
  }
}

/**
 * The "slow" code path for [Lifecycle.withStateAtLeast] operations that requires allocating
 * and suspending, factored into a non-inlined function to avoid inflating code size at call sites
 * or exposing too many implementation details as inlined code.
 */
@PublishedApi
internal suspend fun <R> Lifecycle.suspendWithStateAtLeastUnchecked(
  state: Lifecycle.State,
  dispatchNeeded: Boolean,
  lifecycleDispatcher: CoroutineDispatcher,
  block: () -> R,
): R = suspendCancellableCoroutine { co ->
  val removeObserver = AtomicReference<Lifecycle.Cancellable?>(null)

  val observer = Lifecycle.Observer { event ->
    if (event == Lifecycle.Event.upTo(state)) {
      removeObserver.getAndSet(null)?.cancel()
      co.resumeWith(runCatching(block))
    } else if (event == Lifecycle.Event.ON_DESTROY) {
      removeObserver.getAndSet(null)?.cancel()
      co.resumeWithException(LifecycleDestroyedException())
    }
  }

  if (dispatchNeeded) {
    lifecycleDispatcher.dispatch(
      EmptyCoroutineContext,
      Runnable { removeObserver.set(subscribe(observer)) },
    )
  } else {
    subscribe(observer).also(removeObserver::set)
  }

  co.invokeOnCancellation {
    if (lifecycleDispatcher.isDispatchNeeded(EmptyCoroutineContext)) {
      lifecycleDispatcher.dispatch(
        EmptyCoroutineContext,
        Runnable { removeObserver.getAndSet(null)?.cancel() },
      )
    } else {
      removeObserver.getAndSet(null)?.cancel()
    }
  }
}
