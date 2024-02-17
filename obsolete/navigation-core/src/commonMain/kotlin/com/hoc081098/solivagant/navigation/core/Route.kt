package com.hoc081098.solivagant.navigation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelable
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.jvm.JvmInline
import kotlin.native.HiddenFromObjC
import kotlin.reflect.KClass

/**
 * Represents the route to a destination.
 *
 * The instance of this will be put into the navigation arguments as a [Parcelable] and is then
 * available to the target screens.
 */
@Immutable
public interface Route : Parcelable

public interface Navigator {
  public fun navigateTo(screen: Route)

  public fun navigateBack()
}

/**
 * Represents the content of a route.
 * It is used to map a route to a composable.
 */
public interface RouteContent<T : Route> {
  @Immutable
  @JvmInline
  public value class Id<T : Route>(public val type: KClass<T>)

  /**
   * The id of this content.
   * Basically it is the [KClass] of the route.
   */
  @Stable
  public val id: Id<T>

  /**
   * The composable content of this route.
   */
  @Composable
  public fun Content(route: T)
}

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
public inline fun <reified T : Route> routeContent(noinline content: @Composable (route: T) -> Unit): RouteContent<T> {
  val kClass = kClassOf<T>()

  return object : RouteContent<T> {
    override val id: RouteContent.Id<T> = RouteContent.Id(kClass)

    @Composable
    override fun Content(route: T) = content(route)
  }
}

@Deprecated("Use routeContent instead", ReplaceWith("routeContent(content)"))
public fun <T : Route> routeContent(
  clazz: KClass<T>,
  content: @Composable (route: T) -> Unit,
): RouteContent<T> =
  object : RouteContent<T> {
    override val id: RouteContent.Id<T> = RouteContent.Id(clazz)

    @Composable
    override fun Content(route: T) = content(route)
  }

@PublishedApi
internal inline fun <reified T : Any> kClassOf(): KClass<T> = T::class
