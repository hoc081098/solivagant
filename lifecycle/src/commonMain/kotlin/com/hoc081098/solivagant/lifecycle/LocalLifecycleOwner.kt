package com.hoc081098.solivagant.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

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
    error("No LifecycleOwner was provided")
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

  public infix fun providesDefault(lifecycleOwner: LifecycleOwner): ProvidedValue<LifecycleOwner> =
    LocalLifecycleOwner.providesDefault(lifecycleOwner)
}
