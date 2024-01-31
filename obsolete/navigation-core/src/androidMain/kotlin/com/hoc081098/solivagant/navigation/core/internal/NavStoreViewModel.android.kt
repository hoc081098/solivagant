package com.hoc081098.solivagant.navigation.core.internal

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModelStore as AndroidXViewModelStore
import com.hoc081098.kmp.viewmodel.InternalKmpViewModelApi
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModelStore
import com.hoc081098.solivagant.navigation.core.Route
import com.hoc081098.solivagant.navigation.core.RouteContent

@OptIn(InternalKmpViewModelApi::class)
internal actual fun createViewModelStore(): ViewModelStore =
  ViewModelStore(AndroidXViewModelStore())

@SuppressLint("RestrictedApi")
internal actual fun createSavedStateHandle(id: String, globalSavedStateHandle: SavedStateHandle): SavedStateHandle {
  val restoredBundle = globalSavedStateHandle.get<Bundle>(id)

  return SavedStateHandle
    .createHandle(restoredBundle, null)
    .also { globalSavedStateHandle.setSavedStateProvider(id, it.savedStateProvider()) }
}

@Stable
internal const val SavedStateStack = "com.hoc081098.common.navigation.stack"

internal actual fun setNavStackSavedStateProvider(
  globalSavedStateHandle: SavedStateHandle,
  savedStateFactory: () -> Map<String, ArrayList<out Any>>,
) {
  globalSavedStateHandle.setSavedStateProvider(SavedStateStack) {
    Bundle().apply {
      savedStateFactory().forEach { (k, v) ->
        @Suppress("UNCHECKED_CAST")
        when (val first = v[0]) {
          is String -> putStringArrayList(k, v as ArrayList<String>)
          is Parcelable -> putParcelableArrayList(k, v as ArrayList<Parcelable>)
          else -> error("Unknown type: $first")
        }
      }
    }
  }
}

internal actual fun createNavStack(
  globalSavedStateHandle: SavedStateHandle,
  initialRoute: Route,
  contents: List<RouteContent<*>>,
  onStackEntryRemoved: (NavEntry<*>) -> Unit,
): NavStack {
  val savedState = globalSavedStateHandle.get<Bundle>(SavedStateStack)

  return if (savedState === null) {
    NavStack.create(
      initial = NavEntry.create(
        route = initialRoute,
        contents = contents,
      ),
      onStackEntryRemoved = onStackEntryRemoved,
    )
  } else {
    NavStack.fromSavedState(
      savedState = savedState
        .keySet()
        .associateWith { savedState.get(it) as ArrayList<*> },
      contents = contents,
      onStackEntryRemoved = onStackEntryRemoved,
    )
  }
}

internal actual fun removeSavedStateHandle(
  id: String,
  globalSavedStateHandle: SavedStateHandle,
): Any? = globalSavedStateHandle.run {
  clearSavedStateProvider(id)
  remove<Bundle>(id)
}
