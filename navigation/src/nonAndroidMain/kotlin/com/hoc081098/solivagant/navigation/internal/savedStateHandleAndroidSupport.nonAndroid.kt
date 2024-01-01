package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.kmp.viewmodel.SavedStateHandle

internal actual fun SavedStateHandle.setSavedStateProvider(
  key: String,
  savedStateFactory: () -> Map<String, Any?>,
) {
  // Do nothing
}

internal actual fun SavedStateHandle.removeSavedStateProvider(key: String) {
  // Do nothing
}

internal actual fun SavedStateHandle.getAsMap(key: String): Map<String, Any?>? =
  get(key)

internal actual fun createSavedStateHandleAndSetSavedStateProvider(
  id: String,
  savedStateHandle: SavedStateHandle,
): SavedStateHandle {
  val restoredSavedStateHandle = savedStateHandle.get<SavedStateHandle?>(id)

  return SavedStateHandle()
    .apply {
      restoredSavedStateHandle
        ?.keys()
        ?.forEach { k -> this[k] = restoredSavedStateHandle.get<Any?>(k) }
    }
    .also { savedStateHandle[id] = it }
}
