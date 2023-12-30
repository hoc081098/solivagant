package com.freeletics.khonshu.navigation.internal

import android.os.Parcelable
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.ContractResultOwner
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationResultRequest
import dev.drewhamilton.poko.Poko

@InternalNavigationApi
public sealed interface NavEvent {

    @InternalNavigationApi
    @Poko
    public class NavigateToEvent(
        internal val route: NavRoute,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class NavigateToRootEvent(
        internal val root: NavRoot,
        internal val restoreRootState: Boolean,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class NavigateToActivityEvent(
        internal val route: ActivityRoute,
    ) : NavEvent

    @InternalNavigationApi
    public data object UpEvent : NavEvent

    @InternalNavigationApi
    public data object BackEvent : NavEvent

    @InternalNavigationApi
    @Poko
    public class BackToEvent(
        internal val popUpTo: DestinationId<out BaseRoute>,
        internal val inclusive: Boolean,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class ResetToRoot(
        internal val root: NavRoot,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class ReplaceAll(
        internal val root: NavRoot,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class ActivityResultEvent<I>(
        internal val request: ContractResultOwner<I, *, *>,
        internal val input: I,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class DestinationResultEvent<O : Parcelable>(
        internal val key: NavigationResultRequest.Key<O>,
        internal val result: O,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class MultiNavEvent(
        internal val navEvents: List<NavEvent>,
    ) : NavEvent
}
