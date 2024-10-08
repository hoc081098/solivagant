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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Stable

/**
 * Defaults used in [NavHost]
 */
@Suppress("MagicNumber")
public object NavHostDefaults {
  @Stable
  public fun stackValidationMode(): StackValidationMode = StackValidationMode.Lenient

  @Stable
  public fun transitionAnimations(
    enterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition) =
      { fadeIn(animationSpec = tween(700)) },
    exitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition) =
      { fadeOut(animationSpec = tween(700)) },
    popEnterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition) =
      enterTransition,
    popExitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition) =
      exitTransition,
    replaceEnterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition) =
      enterTransition,
    replaceExitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition) =
      exitTransition,
  ): NavHostTransitionAnimations =
    NavHostTransitionAnimations(
      enterTransition = enterTransition,
      exitTransition = exitTransition,
      popEnterTransition = popEnterTransition,
      popExitTransition = popExitTransition,
      replaceEnterTransition = replaceEnterTransition,
      replaceExitTransition = replaceExitTransition,
    )
}
