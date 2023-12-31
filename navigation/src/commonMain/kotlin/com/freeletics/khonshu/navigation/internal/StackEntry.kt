package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Immutable
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.ContentDestination
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import dev.drewhamilton.poko.Poko

@Poko
@Immutable
internal class StackEntry<T : BaseRoute>(
    val id: Id,
    val route: T,
    val destination: ContentDestination<T>,
) {
    val destinationId
        get() = route.destinationId

    val removable
        // cast is needed for the compiler to recognize that the when is exhaustive
        @Suppress("USELESS_CAST")
        get() = when (route as BaseRoute) {
            is NavRoute -> true
            is NavRoot -> false
        }

    @JvmInline
    value class Id(val value: String)
}
