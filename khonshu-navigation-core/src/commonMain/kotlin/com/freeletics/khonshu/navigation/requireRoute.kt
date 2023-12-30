package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.hoc081098.kmp.viewmodel.SavedStateHandle

public fun <T : BaseRoute> SavedStateHandle.requireRoute(): T {
  return requireNotNull(get<T>(EXTRA_ROUTE)) {
    "SavedStateHandle doesn't contain Route data in \"$EXTRA_ROUTE\""
  }
}

@InternalNavigationApi
public fun BaseRoute.getArguments(): SavedStateHandle =
  SavedStateHandle().also { it[EXTRA_ROUTE] = this }