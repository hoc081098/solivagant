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

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.parcelable.Parcelable

internal expect fun SavedStateHandle.setSavedStateProviderWithParcelable(
  key: String,
  savedStateFactory: () -> Parcelable,
)

internal expect fun SavedStateHandle.removeSavedStateProvider(key: String)

internal expect fun SavedStateHandle.getParcelableFromSavedStateProvider(key: String): Parcelable?

internal expect fun createSavedStateHandleAndSetSavedStateProvider(
  id: String,
  savedStateHandle: SavedStateHandle,
): SavedStateHandle
