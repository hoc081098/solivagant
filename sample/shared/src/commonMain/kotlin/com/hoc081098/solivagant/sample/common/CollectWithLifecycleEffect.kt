package com.hoc081098.solivagant.sample.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Immutable
enum class CollectWithLifecycleEffectDispatcher {
  /**
   * Use [Dispatchers.Main][kotlinx.coroutines.MainCoroutineDispatcher].
   */
  Main,

  /**
   * Use [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate].
   */
  ImmediateMain,

  /**
   * Use [androidx.compose.runtime.Composer.applyCoroutineContext].
   * Under the hood, it uses Compose [androidx.compose.ui.platform.AndroidUiDispatcher].
   */
  Composer,
}

/**
 * Collect the given [Flow] in an effect that runs when [LifecycleOwner.lifecycle] is at least at [minActiveState].
 *
 * - If [dispatcher] is [CollectWithLifecycleEffectDispatcher.ImmediateMain], the effect will run in
 * [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate].
 * - If [dispatcher] is [CollectWithLifecycleEffectDispatcher.Main], the effect will run in
 * [Dispatchers.Main][kotlinx.coroutines.MainCoroutineDispatcher].
 * - If [dispatcher] is [CollectWithLifecycleEffectDispatcher.Composer], the effect will run in
 * [androidx.compose.runtime.Composer.applyCoroutineContext].
 *
 * NOTE: When [dispatcher] or [collector] changes, the effect will **NOT** be restarted.
 * The latest [collector] will be used to receive values from the [Flow] ([rememberUpdatedState] is used).
 * If you want to restart the effect, you need to change [keys].
 *
 * @param keys Keys to be used to [remember] the effect.
 * @param lifecycleOwner The [LifecycleOwner] to be used to [repeatOnLifecycle].
 * @param minActiveState The minimum [Lifecycle.State] to be used to [repeatOnLifecycle].
 * @param dispatcher The dispatcher to be used to launch the [Flow].
 * @param collector The collector to be used to collect the [Flow].
 *
 * @see [LaunchedEffect]
 * @see [CollectWithLifecycleEffectDispatcher]
 */
@Composable
expect fun <T> Flow<T>.CollectWithLifecycleEffect(
  vararg keys: Any?,
  dispatcher: CollectWithLifecycleEffectDispatcher = CollectWithLifecycleEffectDispatcher.ImmediateMain,
  collector: (T) -> Unit,
)

@Composable
expect fun <T> StateFlow<T>.collectAsStateWithLifecycle(
  context: CoroutineContext = EmptyCoroutineContext,
): State<T>
