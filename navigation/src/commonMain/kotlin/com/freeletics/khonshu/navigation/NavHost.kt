package com.freeletics.khonshu.navigation

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.MultiStackNavigationExecutor
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.rememberNavigationExecutor
import java.io.Closeable
import java.lang.ref.WeakReference
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Create a new `NavHost` containing all given [destinations]. [startRoute] will be used as the
 * start destination of the graph. Use [com.freeletics.khonshu.navigation.NavEventNavigator] and
 * [NavigationSetup] to change what is shown in [NavHost].
 *
 * To support deep links a set of [DeepLinkHandlers][DeepLinkHandler] can be passed in optionally.
 * These will be used to build the correct back stack when the current `Activity` was launched with
 * an `ACTION_VIEW` `Intent` that contains an url in it's data. [deepLinkPrefixes] can be used to
 * provide a default set of url patterns that should be matched by any [DeepLinkHandler] that
 * doesn't provide its own [DeepLinkHandler.prefixes].
 *
 * If a [NavEventNavigator] is passed it will be automatically set up and can be used to
 * navigate within the `NavHost`.
 *
 * The [destinationChangedCallback] can be used to be notified when the current destination
 * changes. Note that this will not be invoked when navigating to a [ActivityDestination].
 */
@Composable
public fun NavHost(
    startRoute: NavRoot,
    destinations: ImmutableSet<NavDestination>,
    modifier: Modifier = Modifier,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler> = persistentSetOf(),
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix> = persistentSetOf(),
    navEventNavigator: NavEventNavigator? = null,
    destinationChangedCallback: ((BaseRoute) -> Unit)? = null,
) {
    val executor = rememberNavigationExecutor(startRoute, destinations, deepLinkHandlers, deepLinkPrefixes)

    SystemBackHandling(executor)
    DestinationChangedCallback(executor, destinationChangedCallback)

    val saveableStateHolder = rememberSaveableStateHolder()
    CompositionLocalProvider(LocalNavigationExecutor provides executor) {
        if (navEventNavigator != null) {
            NavigationSetup(navEventNavigator)
        }

        Box(modifier = modifier) {
            executor.visibleEntries.value.forEach { entry ->
                Show(entry, executor, saveableStateHolder)
            }
        }
    }
}

@Composable
private fun <T : BaseRoute> Show(
    entry: StackEntry<T>,
    executor: MultiStackNavigationExecutor,
    saveableStateHolder: SaveableStateHolder,
) {
    // From AndroidX Navigation:
    //   Stash a reference to the SaveableStateHolder in the Store so that
    //   it is available when the destination is cleared. Which, because of animations,
    //   only happens after this leaves composition. Which means we can't rely on
    //   DisposableEffect to clean up this reference (as it'll be cleaned up too early)
    remember(entry, executor, saveableStateHolder) {
        executor.storeFor(entry.id).getOrCreate(SaveableCloseable::class) {
            SaveableCloseable(entry.id.value, WeakReference(saveableStateHolder))
        }
    }

    saveableStateHolder.SaveableStateProvider(entry.id.value) {
        entry.destination.content(entry.route)
    }
}

internal class SaveableCloseable(
    private val id: String,
    private val saveableStateHolderRef: WeakReference<SaveableStateHolder>,
) : Closeable {
    override fun close() {
        saveableStateHolderRef.get()?.removeState(id)
        saveableStateHolderRef.clear()
    }
}

@Composable
private fun SystemBackHandling(executor: MultiStackNavigationExecutor) {
    val backPressedDispatcher = requireNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcher available"
    }

    val callback = remember(executor) {
        object : OnBackPressedCallback(executor.canNavigateBack.value) {
            override fun handleOnBackPressed() {
                executor.navigateBack()
            }
        }
    }

    LaunchedEffect(executor, callback) {
        snapshotFlow { executor.canNavigateBack.value }
            .collect { callback.isEnabled = it }
    }

    DisposableEffect(backPressedDispatcher, callback) {
        backPressedDispatcher.onBackPressedDispatcher.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }
}

@Composable
private fun DestinationChangedCallback(
    executor: MultiStackNavigationExecutor,
    destinationChangedCallback: ((BaseRoute) -> Unit)?,
) {
    if (destinationChangedCallback != null) {
        LaunchedEffect(executor, destinationChangedCallback) {
            snapshotFlow { executor.visibleEntries.value }
                .map { it.last().route }
                .distinctUntilChanged()
                .collect { destinationChangedCallback(it) }
        }
    }
}
