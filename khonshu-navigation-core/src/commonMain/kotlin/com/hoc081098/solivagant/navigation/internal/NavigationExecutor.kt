package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.Navigator
import com.hoc081098.solivagant.navigation.Serializable
import kotlin.reflect.KClass

@InternalNavigationApi
public interface NavigationExecutor : Navigator {
  public fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T
  public fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle
  public fun <T : BaseRoute> savedStateHandleFactoryFor(destinationId: DestinationId<T>): SavedStateHandleFactory
  public fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): Store
  public fun <T : BaseRoute> extra(destinationId: DestinationId<T>): Serializable

  @InternalNavigationApi
  public interface Store {
    public fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T
  }
}
