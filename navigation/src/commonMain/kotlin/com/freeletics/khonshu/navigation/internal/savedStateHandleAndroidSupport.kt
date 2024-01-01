package com.freeletics.khonshu.navigation.internal

import com.hoc081098.kmp.viewmodel.SavedStateHandle

internal expect fun SavedStateHandle.setSavedStateProvider(
  key: String,
  savedStateFactory: () -> Map<String, Any?>,
)

internal expect fun SavedStateHandle.removeSavedStateProvider(key: String)

internal expect fun SavedStateHandle.getAsMap(key: String): Map<String, Any?>?

internal expect fun createSavedStateHandleAndSetSavedStateProvider(
  id: String,
  savedStateHandle: SavedStateHandle,
): SavedStateHandle
