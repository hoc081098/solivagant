package com.freeletics.khonshu.navigation.internal

import android.content.Context
import android.content.Intent
import com.freeletics.khonshu.navigation.ActivityDestination
import com.freeletics.khonshu.navigation.ActivityRoute

internal class ActivityStarter(
    private val context: Context,
    private val activityDestinations: List<ActivityDestination>,
) {
    fun start(route: ActivityRoute) {
        val destination = activityDestinations.find {
            it.id == ActivityDestinationId(route::class)
        }
        requireNotNull(destination) { "Did not find destination for $route" }
        val intent = Intent(destination.intent)
        intent.fillIn(route.fillInIntent(), 0)
        context.startActivity(intent)
    }
}
