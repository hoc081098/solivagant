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
package com.hoc081098.solivagant.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.staticCompositionLocalOf

internal class MissingLifecycleOwnerException : IllegalStateException("No LifecycleOwner was provided")

/**
 * The CompositionLocal containing the current [LifecycleOwner].
 */
@Stable
public object LocalLifecycleOwner {
  /**
   * The CompositionLocal containing the current [LifecycleOwner].
   */
  @Suppress("MemberNameEqualsClassName")
  private val LocalLifecycleOwner = staticCompositionLocalOf<LifecycleOwner> {
    throw MissingLifecycleOwnerException()
  }

  @InternalComposeApi
  @Composable
  @ReadOnlyComposable
  public fun currentOrNull(): LifecycleOwner? =
    currentComposer.run {
      try {
        consume(LocalLifecycleOwner)
      } catch (
        @Suppress("SwallowedException") e: MissingLifecycleOwnerException,
      ) {
        null
      }
    }

  /**
   * Returns current composition local value for the owner.
   * @throws IllegalStateException if no value was provided.
   */
  public val current: LifecycleOwner
    @Composable
    @ReadOnlyComposable
    get() = LocalLifecycleOwner.current

  /**
   * Associates a [LocalLifecycleOwner] key to a value in a call to
   * [CompositionLocalProvider].
   */
  public infix fun provides(lifecycleOwner: LifecycleOwner): ProvidedValue<LifecycleOwner> =
    LocalLifecycleOwner.provides(lifecycleOwner)
}

/**
 * Provides [LifecycleOwner] as [LocalLifecycleOwner] to the [content].
 */
@Composable
public fun LifecycleOwnerProvider(
  lifecycleOwner: LifecycleOwner,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalLifecycleOwner provides lifecycleOwner,
    content = content,
  )
}
