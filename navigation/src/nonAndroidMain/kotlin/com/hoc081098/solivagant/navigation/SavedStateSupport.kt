/*
 * Copyright 2024 Petrus Nguyễn Thái Học
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.hoc081098.kmp.viewmodel.Closeable
import com.hoc081098.kmp.viewmodel.MainThread
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.ViewModelStore
import com.hoc081098.kmp.viewmodel.ViewModelStoreOwner
import com.hoc081098.kmp.viewmodel.compose.LocalSavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.compose.LocalViewModelStoreOwner
import kotlin.LazyThreadSafetyMode.NONE

/**
 * A class helps to support saved state in Compose.
 *
 * The [clear] method will clear all saved state and clear the [ViewModelStore].
 * Usually, you should call [clear] in `onDispose` of a [androidx.compose.runtime.DisposableEffect]
 * that runs in the root @Composable.
 *
 * The [performSave] method will save all the saved state.
 * Usually, you should call [performSave] in `onDispose` of a [androidx.compose.runtime.DisposableEffect]
 * that runs in a child @Composable.
 * That [androidx.compose.runtime.DisposableEffect] should be at the last of the child @Composable,
 * because [SaveableStateRegistry.Entry]s are unregistered in reverse order.
 * We want to save state first, before [SaveableStateRegistry.Entry]s are unregistered.
 *
 * ## Example
 *
 * ```kotlin
 * // Remember a SavedStateSupport instance.
 * val savedStateSupport = remember { SavedStateSupport() }
 *
 * // Clear the SavedStateSupport when the root @Composable exits the composition.
 * savedStateSupport.ClearOnDispose()
 *
 * // Provide SavedStateSupport as ViewModelStoreOwner, SaveableStateRegistry and SavedStateHandleFactory.
 * savedStateSupport.ProvideCompositionLocals {
 *   MyApp()
 * }
 * ```
 */
@MainThread
public class SavedStateSupport :
  SavedStateHandleFactory,
  SaveableStateRegistry,
  ViewModelStoreOwner {
  private var registry: SaveableStateRegistry? = SaveableStateRegistry(
    restoredValues = emptyMap(),
    canBeSaved = { true },
  )

  private val viewModelStoreLazy = lazy(NONE) { ViewModelStore() }
  override val viewModelStore: ViewModelStore by viewModelStoreLazy

  private val closeables: LinkedHashMap<Any, Closeable> = linkedMapOf<Any, Closeable>() // preserve order

  /**
   * Add a [Closeable] that will be closed when [clear] is called.
   *
   * @param key The key to identify the [Closeable].
   * @param strategy Determine when to close the [Closeable].
   * @param closeable The [Closeable] to add.
   */
  public fun addCloseable(
    key: Any,
    strategy: CloseableStrategy = CloseableStrategy.CloseIfChanged,
    closeable: Closeable,
  ) {
    if (registry == null) {
      return closeable.close()
    }

    val oldClosable = closeables[key]
    when (strategy) {
      CloseableStrategy.CloseIfChanged -> {
        when {
          oldClosable == null -> {
            closeables[key] = closeable
          }

          oldClosable != closeable -> {
            oldClosable.close()
            closeables[key] = closeable
          }

          else -> Unit
        }
      }

      CloseableStrategy.AlwaysClose -> {
        oldClosable?.close()

        if (oldClosable != closeable) {
          closeables[key] = closeable
        }
      }

      CloseableStrategy.DoNothing -> {
        closeables[key] = closeable
      }
    }
  }

  /**
   * Get a [Closeable] by key.
   */
  public fun getCloseable(key: Any): Closeable? = closeables[key]

  /**
   * Remove a [Closeable] by key and close it if [close] is `true`.
   */
  public fun removeCloseable(
    key: Any,
    close: Boolean = true,
  ): Closeable? =
    closeables
      .remove(key)
      ?.apply { if (close) close() }

  public fun clear() {
    if (registry == null) {
      // Already cleared
      return
    }

    registry = null
    if (viewModelStoreLazy.isInitialized()) {
      viewModelStore.clear()
    }
    closeables.values.forEach { it.close() }
    closeables.clear()
  }

  override fun create(): SavedStateHandle = SavedStateHandle()

  override fun canBeSaved(value: Any): Boolean = registry?.canBeSaved(value) == true

  override fun consumeRestored(key: String): Any? = registry?.consumeRestored(key)

  override fun performSave(): Map<String, List<Any?>> =
    registry
      ?.performSave()
      ?.also {
        registry = SaveableStateRegistry(
          restoredValues = it,
          canBeSaved = { true },
        )
      }
      .orEmpty()

  override fun registerProvider(
    key: String,
    valueProvider: () -> Any?,
  ): SaveableStateRegistry.Entry =
    registry
      ?.registerProvider(key, valueProvider)
      ?: NoOpSaveableStateRegistryEntry

  /**
   * Determine when to close a [Closeable].
   */
  public enum class CloseableStrategy {
    /**
     * Close the [Closeable] if it's changed.
     */
    CloseIfChanged,

    /**
     * Always close the [Closeable].
     */
    AlwaysClose,

    /**
     * Do nothing.
     */
    DoNothing,
  }
}

private val NoOpSaveableStateRegistryEntry = object : SaveableStateRegistry.Entry {
  override fun unregister() {
    // No-op
  }
}

/**
 * Clear this [SavedStateSupport] when the @Composable exits the composition.
 */
@Composable
@NonRestartableComposable
public fun SavedStateSupport.ClearOnDispose(): Unit =
  DisposableEffect(Unit) {
    onDispose(::clear)
  }

/**
 * Provides [this] [SavedStateSupport] as [LocalViewModelStoreOwner], [LocalSaveableStateRegistry] and
 * [LocalSavedStateHandleFactory] to the [content],
 * along with other [ProvidedValue]s.
 *
 * The [SavedStateSupport.performSave] method will be called in a [DisposableEffect]
 * placed after the [content]. This makes sure that the state is saved before [SaveableStateRegistry.Entry]s are
 * unregistered.
 * Therefore, all [androidx.compose.runtime.saveable.rememberSaveable]s used in the [content] will be saved.
 *
 * @param others Other [ProvidedValue]s.
 * @param content The content [Composable]
 */
@Composable
public fun SavedStateSupport.ProvideCompositionLocals(
  vararg others: ProvidedValue<*>,
  content: @Composable () -> Unit,
): Unit =
  CompositionLocalProvider(
    *others,
    LocalViewModelStoreOwner provides this,
    LocalSaveableStateRegistry provides this,
    LocalSavedStateHandleFactory provides this,
  ) {
    content()

    // Must be at the last,
    // because onDispose is called in reverse order, so we want to save state first,
    // before [SaveableStateRegistry.Entry]s are unregistered.
    DisposableEffect(Unit) {
      onDispose(::performSave)
    }
  }
