package com.freeletics.khonshu.navigation.internal

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle

internal actual fun SavedStateHandle.setSavedStateProvider(
  key: String,
  savedStateFactory: () -> Map<String, Any?>,
) = setSavedStateProvider(key) {
  Bundle().apply {
    savedStateFactory().forEach { (k, v) ->
      when (v) {
        null -> putString(k, null)
        is Parcelable -> putParcelable(k, v)
        is ArrayList<*> -> {
          @Suppress("UNCHECKED_CAST")
          v as ArrayList<Map<String, ArrayList<out Any>>>

          putParcelableArrayList(
            k,
            ArrayList<Parcelable>(v.size)
              .apply { v.forEach { add(it.toBundle()) } },
          )
        }

        else -> error("Unknown type: $v")
      }
    }
  }
}

private fun Map<String, Any?>.toBundle(): Bundle = Bundle().apply {
  forEach { (k, v) ->
    when (v) {
      null -> putString(k, null)
      is Parcelable -> putParcelable(k, v)
      is ArrayList<*> -> {
        @Suppress("UNCHECKED_CAST")
        when (val first = v[0]) {
          is String -> putStringArrayList(k, v as ArrayList<String>)
          is Parcelable -> putParcelableArrayList(k, v as ArrayList<Parcelable>)
          else -> error("Unknown type: $first")
        }
      }

      else -> error("Unknown type: $v")
    }
  }
}

internal actual fun SavedStateHandle.removeSavedStateProvider(key: String) =
  clearSavedStateProvider(key)

internal actual fun SavedStateHandle.getAsMap(key: String): Map<String, Any?>? =
  get<Bundle?>(key)
    ?.let { bundle ->
      bundle
        .keySet()
        .associateWith { bundle.get(it) }
    }

@SuppressLint("RestrictedApi")
internal actual fun createSavedStateHandleAndSetSavedStateProvider(
  id: String,
  savedStateHandle: SavedStateHandle,
): SavedStateHandle {
  val restoredBundle = savedStateHandle.get<Bundle?>(id)

  return SavedStateHandle
    .createHandle(restoredBundle, null)
    .also { savedStateHandle.setSavedStateProvider(id, it.savedStateProvider()) }
}
