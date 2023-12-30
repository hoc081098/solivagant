package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.Navigator
import com.freeletics.khonshu.navigation.Serializable
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import kotlin.reflect.KClass

@InternalNavigationApi
public interface NavigationExecutor : Navigator {
    public fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T
    public fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle
    public fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): Store
    public fun <T : BaseRoute> extra(destinationId: DestinationId<T>): Serializable

    @InternalNavigationApi
    public interface Store {
        public fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T
    }
}
