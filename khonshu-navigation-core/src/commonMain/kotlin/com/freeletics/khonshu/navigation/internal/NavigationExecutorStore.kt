package com.freeletics.khonshu.navigation.internal

import java.io.Closeable
import kotlin.reflect.KClass

@InternalNavigationApi
public class NavigationExecutorStore : NavigationExecutor.Store, Closeable {
    private val storedObjects = mutableMapOf<KClass<*>, Any>()

    override fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        var storedObject = storedObjects[key] as T?
        if (storedObject == null) {
            storedObject = factory()
            storedObjects[key] = storedObject
        }
        return storedObject
    }

    override fun close() {
        storedObjects.forEach { (_, storedObject) ->
            if (storedObject is Closeable) {
                storedObject.close()
            }
        }
        storedObjects.clear()
    }
}
