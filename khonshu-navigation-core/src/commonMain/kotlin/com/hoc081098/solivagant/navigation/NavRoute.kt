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

import androidx.compose.runtime.Immutable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelable

@Immutable
public sealed interface BaseRoute : Parcelable

/**
 * Represents the route to a destination.
 *
 * The instance of this will be put into the navigation arguments as a [Parcelable] and is then
 * available to the target screens.
 */
@Immutable
public interface NavRoute : BaseRoute

/**
 * This is similar to a [NavRoute] but represents the route to the start destination used in
 * a backstack. When you navigate to a [NavRoot] the current backstack is saved and removed
 * so that the [NavRoot] is right on top of the start destination.
 *
 * The instance of this will be put into the navigation arguments as a [Parcelable] and is then
 * available to the target screens.
 */
@Immutable
public interface NavRoot : BaseRoute
