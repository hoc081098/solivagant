package com.hoc081098.solivagant.navigation.internal

import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.State
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.SavedStateHandleFactory
import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.lifecycle.eventFlow
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import com.hoc081098.solivagant.navigation.Serializable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
internal class MultiStackNavigationExecutor(
  private val stack: MultiStack,
  private val viewModel: StoreViewModel,
  private val onRootChanged: (NavRoot) -> Unit,
) : NavigationExecutor, RememberObserver {
  private val scope = viewModel.viewModelScope + Job()
  private val _lifecycleOwner = MutableStateFlow<LifecycleOwner?>(null)

  val visibleEntries: State<ImmutableList<StackEntry<*>>>
    get() = stack.visibleEntries

  val canNavigateBack: State<Boolean>
    get() = stack.canNavigateBack

  init {
    _lifecycleOwner
      .flatMapLatest { it?.lifecycle?.eventFlow ?: emptyFlow() }
      .onEach(stack::handleLifecycleEvent)
      .launchIn(scope)
  }

  override fun navigateTo(route: NavRoute) {
    stack.push(route)
  }

  override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
    stack.push(root, clearTargetStack = !restoreRootState)
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
    return viewModel.provideSavedStateHandle(entry.id, entry.route)
  }

  override fun <T : BaseRoute> savedStateHandleFactoryFor(destinationId: DestinationId<T>): SavedStateHandleFactory {
    val entry = entryFor(destinationId)
    return viewModel.provideSavedStateHandleFactory(entry.id, entry.route)
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
      ?: error("Route $destinationId not found on back stack")
  }

  fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
    _lifecycleOwner.value = lifecycleOwner
  }

  //region RememberObserver
  override fun onAbandoned() {
    _lifecycleOwner.value = null
    scope.cancel()
  }

  override fun onForgotten() {
    _lifecycleOwner.value = null
    scope.cancel()
  }

  override fun onRemembered() = Unit
  //endregion
}
