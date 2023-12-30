package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hoc081098.solivagant.navigation.BaseRoute
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

@InternalNavigationApi
@JvmInline
@Immutable
public value class DestinationId<T : BaseRoute>(public val route: KClass<T>)

@InternalNavigationApi
@Stable
public val <T : BaseRoute> T.destinationId: DestinationId<out T>
    get() = DestinationId(this::class)

