package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelable
import com.hoc081098.kmp.viewmodel.parcelable.Parceler
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.kmp.viewmodel.parcelable.WriteWith
import com.hoc081098.solivagant.navigation.BaseRoute
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

// TODO: inline value class
@InternalNavigationApi
@Immutable
@Parcelize
public data class DestinationId<T : BaseRoute>(
  @JvmField public val route: @WriteWith<KClassParceler> KClass<T>,
) : Parcelable

@InternalNavigationApi
@Stable
public val <T : BaseRoute> T.destinationId: DestinationId<out T>
  get() = DestinationId(this::class)

internal expect object KClassParceler : Parceler<KClass<*>>
