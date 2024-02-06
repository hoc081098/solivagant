package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.Navigator
import com.hoc081098.solivagant.navigation.Serializable
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * A unique identifier for an entry in the navigation stack.
 */
@InternalNavigationApi
@JvmInline
public value class StackEntryId(public val value: String)

/**
 * An internal API for navigation.
 */
@InternalNavigationApi
public interface NavigationExecutor : Navigator {
  /**
   * Find the [StackEntryId] by the given [route].
   */
  public fun stackEntryIdFor(route: BaseRoute): StackEntryId

  /**
   * Find the [StackEntryId] by the given [destinationId].
   */
  @Deprecated("Should not use destinationId directly, use route instead.")
  public fun stackEntryIdFor(destinationId: DestinationId<*>): StackEntryId

  /**
   * Returns the [SavedStateHandle] for the given [id].
   */
  public fun savedStateHandleFor(id: StackEntryId): SavedStateHandle

  /**
   * Returns the [SavedStateHandleFactory] for the given [id].
   */
  public fun savedStateHandleFactoryFor(id: StackEntryId): SavedStateHandleFactory

  /**
   * Returns the [Store] for the given [id].
   */
  public fun storeFor(id: StackEntryId): Store

  /**
   * Returns the extra data for the given [id].
   */
  public fun extra(id: StackEntryId): Serializable

  @InternalNavigationApi
  public interface Store {
    public fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T
  }
}
