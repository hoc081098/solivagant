package com.freeletics.khonshu.navigation.internal

import android.os.Parcelable
import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import java.io.Serializable
import kotlinx.collections.immutable.ImmutableList

internal class MultiStackNavigationExecutor(
    private val stack: MultiStack,
    private val viewModel: StoreViewModel,
    private val activityStarter: (ActivityRoute) -> Unit,
    private val onRootChanged: (NavRoot) -> Unit,
    deepLinkRoutes: List<Parcelable>,
) : NavigationExecutor {

    val visibleEntries: State<ImmutableList<StackEntry<*>>>
        get() = stack.visibleEntries

    val canNavigateBack: State<Boolean>
        get() = stack.canNavigateBack

    init {
        if (deepLinkRoutes.isNotEmpty()) {
            stack.resetToRoot(stack.startRoot)
            deepLinkRoutes.forEachIndexed { index, route ->
                when (route) {
                    is NavRoot -> {
                        require(index == 0) { "NavRoot can only be the first element of a deep link" }
                        require(route.destinationId != stack.startRoot.destinationId) {
                            "$route is the start root which is not allowed to be part of a deep " +
                                "link because it will always be on the back stack"
                        }
                        stack.push(route, clearTargetStack = true)
                    }

                    is NavRoute -> stack.push(route)
                    is ActivityRoute -> navigateTo(route)
                }
            }
        }

        viewModel.globalSavedStateHandle.setSavedStateProvider(SAVED_STATE_STACK) {
            val stackState = stack.saveState()
            // if this line is reached the stackState contains the deep links already
            stackState.putBoolean(SAVED_STATE_HANDLED_DEEP_LINKS, true)
            stackState
        }
    }

    override fun navigateTo(route: NavRoute) {
        stack.push(route)
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        stack.push(root, clearTargetStack = !restoreRootState)
    }

    override fun navigateTo(route: ActivityRoute) {
        activityStarter(route)
    }

    override fun navigateUp() {
        stack.popCurrentStack()
    }

    override fun navigateBack() {
        stack.pop()
    }

    override fun <T : BaseRoute> navigateBackToInternal(
        popUpTo: DestinationId<T>,
        inclusive: Boolean,
    ) {
        stack.popUpTo(popUpTo, inclusive)
    }

    override fun resetToRoot(root: NavRoot) {
        stack.resetToRoot(root)
    }

    override fun replaceAll(root: NavRoot) {
        stack.replaceAll(root)
        onRootChanged(root)
    }

    override fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T {
        return entryFor(destinationId).route
    }

    override fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle {
        val entry = entryFor(destinationId)
        return viewModel.provideSavedStateHandle(entry.id)
    }

    override fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): NavigationExecutor.Store {
        val entry = entryFor(destinationId)
        return storeFor(entry.id)
    }

    override fun <T : BaseRoute> extra(destinationId: DestinationId<T>): Serializable {
        val entry = entryFor(destinationId)
        return entry.destination.extra!!
    }

    internal fun storeFor(entryId: StackEntry.Id): NavigationExecutor.Store {
        return viewModel.provideStore(entryId)
    }

    private fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T> {
        return stack.entryFor(destinationId)
            ?: throw IllegalStateException("Route $destinationId not found on back stack")
    }

    internal companion object {
        const val SAVED_STATE_STACK = "com.freeletics.khonshu.navigation.stack"
        const val SAVED_STATE_HANDLED_DEEP_LINKS = "com.freeletics.khonshu.navigation.handled_deep_links"
    }
}
