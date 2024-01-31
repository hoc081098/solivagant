package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.kmp.viewmodel.ViewModelStore
import com.hoc081098.kmp.viewmodel.ViewModelStoreOwner
import kotlin.LazyThreadSafetyMode.NONE

public class SavedStateSupport :
  SavedStateHandleFactory,
  SaveableStateRegistry,
  ViewModelStoreOwner {

  private var restoredValues: Map<String, List<Any?>> = emptyMap()
  private var registry = SaveableStateRegistry(
    restoredValues = restoredValues,
    canBeSaved = { true },
  )

  private val viewModelStoreLazy = lazy(NONE) { ViewModelStore() }
  private val savedStateHandle by lazy(NONE) { SavedStateHandle() }

  override val viewModelStore: ViewModelStore by viewModelStoreLazy

  public fun clear() {
    restoredValues = emptyMap()
    savedStateHandle.keys().forEach { savedStateHandle.remove<Any?>(it) }
    if (viewModelStoreLazy.isInitialized()) {
      viewModelStore.clear()
    }
  }

  override fun create(): SavedStateHandle = savedStateHandle

  override fun canBeSaved(value: Any): Boolean = registry.canBeSaved(value)
    .also {
      println("canBeSaved: value=$value -> $it")
      println("canBeSaved: restoredValues=$restoredValues, registry=$registry")
    }

  override fun consumeRestored(key: String): Any? = registry.consumeRestored(key)
    .also {
      println("consumeRestored: key=$key -> $it")
      println("consumeRestored: restoredValues=$restoredValues, registry=$registry")
    }

  override fun performSave(): Map<String, List<Any?>> {
    val listMap = registry.performSave()
    val savedStateHandleMap = savedStateHandle.keys().associateWith { savedStateHandle.get<Any?>(it) }
    val finalMap = listMap + mapOf(savedStateHandle.toString() to listOf(savedStateHandleMap))

    println("performSave: restoredValues=$restoredValues, registry=$registry")

    restoredValues = finalMap
    registry = SaveableStateRegistry(
      restoredValues = finalMap,
      canBeSaved = { true },
    )

    println("performSave: finalMap=$finalMap")
    println("performSave: restoredValues=$restoredValues, registry=$registry")

    return finalMap
  }

  override fun registerProvider(key: String, valueProvider: () -> Any?): SaveableStateRegistry.Entry =
    registry.registerProvider(key, valueProvider)
      .also {
        println("registerProvider: key=$key, valueProvider=$valueProvider -> $it")
        println("registerProvider: restoredValues=$restoredValues, registry=$registry")
      }
}
