package com.freeletics.khonshu.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import com.freeletics.khonshu.navigation.internal.ActivityDestinationId
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import java.io.Serializable

/**
 * A destination that can be navigated to. See `NavHost` for how to configure a `NavGraph` with it.
 */
public sealed interface NavDestination

@InternalNavigationApi
public sealed interface ContentDestination<T : BaseRoute> : NavDestination {
    @InternalNavigationApi
    public val id: DestinationId<T>

    @InternalNavigationApi
    public val extra: Serializable?

    @InternalNavigationApi
    public val content: @Composable (T) -> Unit
}

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier. The given [content] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : BaseRoute> ScreenDestination(
    noinline content: @Composable (T) -> Unit,
): NavDestination = ScreenDestination(DestinationId(T::class), null, content)

@InternalNavigationApi
@Suppress("FunctionName")
public inline fun <reified T : BaseRoute> ScreenDestination(
    extra: Serializable,
    noinline content: @Composable (T) -> Unit,
): NavDestination = ScreenDestination(DestinationId(T::class), extra, content)

@InternalNavigationApi
public class ScreenDestination<T : BaseRoute>(
    override val id: DestinationId<T>,
    override val extra: Serializable?,
    override val content: @Composable (T) -> Unit,
) : ContentDestination<T>

/**
 * Creates a new [NavDestination] that is shown on top a [ScreenDestination], for example a dialog or bottom sheet. The
 * class of [T] will be used as a unique identifier. The given [content] will be shown inside the dialog window when
 * navigating to it by using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> OverlayDestination(
    noinline content: @Composable (T) -> Unit,
): NavDestination = OverlayDestination(DestinationId(T::class), null, content)

@InternalNavigationApi
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> OverlayDestination(
    extra: Serializable,
    noinline content: @Composable (T) -> Unit,
): NavDestination = OverlayDestination(DestinationId(T::class), extra, content)

@InternalNavigationApi
public class OverlayDestination<T : NavRoute>(
    override val id: DestinationId<T>,
    override val extra: Serializable?,
    override val content: @Composable (T) -> Unit,
) : ContentDestination<T>

/**
 * Creates a new [NavDestination] that represents an `Activity`. The class of [T] will be used
 * as a unique identifier. The given [intent] will be used to launch the `Activity` when using an
 * instance of [T] for navigation.
 */
@Suppress("FunctionName")
public inline fun <reified T : ActivityRoute> ActivityDestination(
    intent: Intent,
): NavDestination = ActivityDestination(ActivityDestinationId(T::class), intent)

@InternalNavigationApi
public class ActivityDestination(
    @property:InternalNavigationApi
    public val id: ActivityDestinationId<out ActivityRoute>,
    @property:InternalNavigationApi
    public val intent: Intent,
) : NavDestination
