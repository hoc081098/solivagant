package com.hoc081098.solivagant.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Stable
import dev.drewhamilton.poko.Poko

/**
 * Defaults used in [NavHost]
 */
@Suppress("MagicNumber")
public object NavHostDefaults {
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
  ): NavHostTransitionAnimations = NavHostTransitionAnimations(
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    replaceEnterTransition = replaceEnterTransition,
    replaceExitTransition = replaceExitTransition,
  )
}

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
    public fun noAnimations(): NavHostTransitionAnimations = NavHostTransitionAnimations(
      enterTransition = { EnterTransition.None },
      exitTransition = { ExitTransition.None },
      popEnterTransition = { EnterTransition.None },
      popExitTransition = { ExitTransition.None },
      replaceEnterTransition = { EnterTransition.None },
      replaceExitTransition = { ExitTransition.None },
    )
  }
}
