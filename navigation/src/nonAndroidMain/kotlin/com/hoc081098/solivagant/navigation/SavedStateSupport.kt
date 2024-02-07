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

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.hoc081098.kmp.viewmodel.MainThread
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.ViewModelStore
import com.hoc081098.kmp.viewmodel.ViewModelStoreOwner
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
 * DisposableEffect(Unit) {
 *   onDispose(savedStateSupport::clear)
 * }
 *
 * // Provide SavedStateSupport as ViewModelStoreOwner, SaveableStateRegistry and SavedStateHandleFactory.
 * CompositionLocalProvider(
 *   LocalViewModelStoreOwner provides savedStateSupport,
 *   LocalSaveableStateRegistry provides savedStateSupport,
 *   LocalSavedStateHandleFactory provides savedStateSupport,
 * ) {
 *   MyApp()
 *
 *   // Must be at the last,
 *   // because onDispose is called in reverse order, so we want to save state first,
 *   // before [SaveableStateRegistry.Entry]s are unregistered.
 *   DisposableEffect(Unit) {
 *     onDispose(savedStateSupport::performSave)
 *   }
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

  private val savedStateHandle by lazy(NONE) { SavedStateHandle() }
  private val viewModelStoreLazy = lazy(NONE) { ViewModelStore() }
  override val viewModelStore: ViewModelStore by viewModelStoreLazy

  public fun clear() {
    if (registry == null) {
      // Already cleared
      return
    }

    registry = null
    if (viewModelStoreLazy.isInitialized()) {
      viewModelStore.clear()
    }
  }

  override fun create(): SavedStateHandle = savedStateHandle

  override fun canBeSaved(value: Any): Boolean = registry?.canBeSaved(value) == true

  override fun consumeRestored(key: String): Any? = registry?.consumeRestored(key)

  override fun performSave(): Map<String, List<Any?>> = registry
    ?.performSave()
    ?.also {
      registry = SaveableStateRegistry(
        restoredValues = it,
        canBeSaved = { true },
      )
    }
    .orEmpty()

  override fun registerProvider(key: String, valueProvider: () -> Any?): SaveableStateRegistry.Entry =
    registry
      ?.registerProvider(key, valueProvider)
      ?: NoOpSaveableStateRegistryEntry
}

private val NoOpSaveableStateRegistryEntry = object : SaveableStateRegistry.Entry {
  override fun unregister() {
    // No-op
  }
}
