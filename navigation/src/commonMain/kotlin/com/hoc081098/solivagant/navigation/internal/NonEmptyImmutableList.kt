package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.adapters.ImmutableListAdapter

@Immutable
@JvmInline
internal value class NonEmptyImmutableList<E>(
  val list: ImmutableList<E>,
) : ImmutableList<E> by list {
  init {
    require(list.isNotEmpty()) { "List must not be empty" }
  }

  inline fun first(): E = list[0]

  companion object {
    @JvmStatic
    fun <E> adapt(list: List<E>): NonEmptyImmutableList<E> {
      require(list.isNotEmpty()) { "List must not be empty" }
      return NonEmptyImmutableList(ImmutableListAdapter(list))
    }
  }
}
