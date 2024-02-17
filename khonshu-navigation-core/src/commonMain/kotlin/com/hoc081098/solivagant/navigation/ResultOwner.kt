/*
 * Copyright 2021 Freeletics GmbH.
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

import com.hoc081098.kmp.viewmodel.parcelable.Parcelable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.internal.DestinationId
import com.hoc081098.solivagant.navigation.internal.trySendBlocking
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A base class for anything that exposes a [Flow] of [results]. Results will only be delivered
 * to one collector at a time.
 */
public sealed class ResultOwner<R> {
  /**
   * Emits any result passed to [onResult]. Results will only be delivered
   * to one collector at a time.
   */
  private val _results = Channel<R>(capacity = Channel.UNLIMITED)

  public val results: Flow<R> = _results.receiveAsFlow()

  /**
   * Deliver a new [result] to [results]. This method should be called by a
   * `NavEventNavigationHandler`.
   */
  @InternalNavigationApi
  public fun onResult(result: R) {
    val channelResult = _results.trySendBlocking(result)
    check(channelResult.isSuccess)
  }
}

/**
 * Class that exposes a [results] [Flow] that can be used to observe results for
 * the given [key].
 *
 * See [ResultOwner] and [NavEventNavigator.registerForNavigationResult].
 */
public class NavigationResultRequest<R : Parcelable> internal constructor(
  public val key: Key<R>,
) : ResultOwner<R>() {
  /**
   * Use to identify where the result should be delivered to.
   */
  @Poko
  @Parcelize
  public class Key<R : Parcelable>
    @InternalNavigationApi
    constructor(
      internal val destinationId: DestinationId<*>,
      internal val requestKey: String,
    ) : Parcelable
}
