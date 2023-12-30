package com.freeletics.khonshu.navigation

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi

/**
 * Represents the route to an `Activity`. Should be used through []
 */
public sealed interface ActivityRoute : Parcelable {
    public fun fillInIntent(): Intent
}

/**
 * Represents the route to an `Activity` within the current app. The instance of this route
 * will be added to the resulting `Intent` and can be accessed in the launched `Activity` by calling
 * [getRoute] or [requireRoute].
 */
public abstract class InternalActivityRoute : ActivityRoute {
    final override fun fillInIntent(): Intent {
        return Intent().putExtra(EXTRA_ROUTE, this)
    }
}

/**
 * Represents the route to an `Activity` in another app. [fillInIntent] can be used to dynamically
 * add extras to the resulting `Intent`.
 */
public interface ExternalActivityRoute : ActivityRoute {
    override fun fillInIntent(): Intent {
        return Intent()
    }
}

/**
 * Returns the [ActivityRoute] that was used to navigate to this [Activity].
 */
public fun <T : InternalActivityRoute> Activity.requireRoute(): T {
    return requireNotNull(getRoute()) {
        "Error extracting ActivityRoute from Activity's Intent"
    }
}

/**
 * Returns the [ActivityRoute] that was used to navigate to this [Activity] if it's present in
 * Activity's Intent.
 */
public fun <T : InternalActivityRoute> Activity.getRoute(): T? {
    @Suppress("DEPRECATION")
    return intent.extras?.getParcelable(EXTRA_ROUTE)
}

@InternalNavigationApi
public const val EXTRA_ROUTE: String = "com.freeletics.khonshu.navigation.ROUTE"
