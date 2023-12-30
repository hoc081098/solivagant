package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import kotlin.reflect.KClass

@InternalNavigationApi
@JvmInline
public value class DestinationId<T : BaseRoute>(public val route: KClass<T>)

@InternalNavigationApi
public val <T : BaseRoute> T.destinationId: DestinationId<out T>
    get() = DestinationId(this::class)

@InternalNavigationApi
@JvmInline
public value class ActivityDestinationId<T : ActivityRoute>(public val route: KClass<T>)
