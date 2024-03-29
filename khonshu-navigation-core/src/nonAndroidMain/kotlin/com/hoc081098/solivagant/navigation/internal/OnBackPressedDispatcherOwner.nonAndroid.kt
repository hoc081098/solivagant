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

package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Composable
import com.hoc081098.solivagant.navigation.InternalNavigationApi

@InternalNavigationApi
public actual class OnBackPressedDispatcherOwner {
  public actual fun addCallback(callback: OnBackPressedCallback) {
    // no-op
  }

  public companion object {
    internal val instance by lazy { OnBackPressedDispatcherOwner() }
  }
}

@InternalNavigationApi
@Composable
public actual fun currentBackPressedDispatcher(): OnBackPressedDispatcherOwner = OnBackPressedDispatcherOwner.instance
