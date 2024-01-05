package com.hoc081098.solivagant.sample.common

import androidx.lifecycle.compose.collectAsStateWithLifecycle as androidXCollectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
actual fun <T> Flow<T>.CollectWithLifecycleEffect(
  vararg keys: Any?,
  dispatcher: CollectWithLifecycleEffectDispatcher,
  collector: (T) -> Unit
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val minActiveState = Lifecycle.State.STARTED

  val flow = this
  val collectorState = rememberUpdatedState(collector)

  val block: suspend CoroutineScope.() -> Unit = {
    lifecycleOwner.repeatOnLifecycle(minActiveState) {
      // NOTE: we don't use `flow.collect(collectState.value)` because it can use the old value
      flow.collect { collectorState.value(it) }
    }
  }

  when (dispatcher) {
    CollectWithLifecycleEffectDispatcher.ImmediateMain -> {
      LaunchedEffectInImmediateMain(flow, lifecycleOwner, minActiveState, *keys, block = block)
    }

    CollectWithLifecycleEffectDispatcher.Main -> {
      LaunchedEffectInMain(flow, lifecycleOwner, minActiveState, *keys, block = block)
    }

    CollectWithLifecycleEffectDispatcher.Composer -> {
      LaunchedEffect(flow, lifecycleOwner, minActiveState, *keys, block = block)
    }
  }
}

@Composable
actual fun <T> StateFlow<T>.collectAsStateWithLifecycle(
  context: CoroutineContext
): State<T> = androidXCollectAsStateWithLifecycle(context = context)


@Composable
@NonRestartableComposable
@Suppress("ArrayReturn")
private fun LaunchedEffectInImmediateMain(
  vararg keys: Any?,
  block: suspend CoroutineScope.() -> Unit,
) {
  remember(*keys) { LaunchedEffectImpl(block, Dispatchers.Main.immediate) }
}

@Composable
@NonRestartableComposable
@Suppress("ArrayReturn")
private fun LaunchedEffectInMain(
  vararg keys: Any?,
  block: suspend CoroutineScope.() -> Unit,
) {
  remember(*keys) { LaunchedEffectImpl(block, Dispatchers.Main) }
}

private class LaunchedEffectImpl(
  private val task: suspend CoroutineScope.() -> Unit,
  dispatcher: CoroutineDispatcher,
) : RememberObserver {
  private val scope = CoroutineScope(dispatcher)
  private var job: Job? = null

  override fun onRemembered() {
    job?.cancel("Old job was still running!")
    job = scope.launch(block = task)
  }

  override fun onForgotten() {
    job?.cancel()
    job = null
  }

  override fun onAbandoned() {
    job?.cancel()
    job = null
  }
}
