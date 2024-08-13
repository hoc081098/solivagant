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
import androidx.compose.runtime.Stable
import dev.drewhamilton.poko.Poko

/**
 * Represents set of callbacks to define transition animations for destinations in [NavHost]
 */
@Stable
@Poko
public class NavHostTransitionAnimations internal constructor(
  public val enterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition),
  public val exitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition),
  public val popEnterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition),
  public val popExitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition),
  public val replaceEnterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition),
  public val replaceExitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition),
) {
  public companion object {
    /**
     * Disables all transition animations
     */
    @Stable
    public fun noAnimations(): NavHostTransitionAnimations =
      NavHostTransitionAnimations(
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        replaceEnterTransition = { EnterTransition.None },
        replaceExitTransition = { ExitTransition.None },
      )
  }
}
