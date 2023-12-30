package com.hoc081098.solivagant.navigation.internal

import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.Navigator

internal class NavEventCollector : Navigator {

    private val _navEvents = mutableListOf<NavEvent>()
    internal val navEvents: List<NavEvent> = _navEvents

    override fun navigateTo(route: NavRoute) {
        val event = NavEvent.NavigateToEvent(route)
        _navEvents.add(event)
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        _navEvents.add(event)
    }

    override fun navigateUp() {
        val event = NavEvent.UpEvent
        _navEvents.add(event)
    }

    override fun navigateBack() {
        val event = NavEvent.BackEvent
        _navEvents.add(event)
    }

    @InternalNavigationApi
    override fun <T : BaseRoute> navigateBackToInternal(popUpTo: DestinationId<T>, inclusive: Boolean) {
        val event = NavEvent.BackToEvent(popUpTo, inclusive)
        _navEvents.add(event)
    }

    override fun resetToRoot(root: NavRoot) {
        val event = NavEvent.ResetToRoot(root)
        _navEvents.add(event)
    }

    override fun replaceAll(root: NavRoot) {
        val event = NavEvent.ReplaceAll(root)
        _navEvents.add(event)
    }
}
