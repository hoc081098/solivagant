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

import com.hoc081098.kmp.viewmodel.ViewModelStore
import com.hoc081098.kmp.viewmodel.ViewModelStoreOwner
import kotlin.LazyThreadSafetyMode.NONE

internal expect fun createViewModelStore(): ViewModelStore

internal class StackEntryViewModelStoreOwner : ViewModelStoreOwner {
  private val viewModelStoreLazy = lazy(NONE) { createViewModelStore() }

  fun clearIfInitialized() {
    if (viewModelStoreLazy.isInitialized()) {
      viewModelStoreLazy.value.clear()
    }
  }

  override val viewModelStore: ViewModelStore by viewModelStoreLazy
}
