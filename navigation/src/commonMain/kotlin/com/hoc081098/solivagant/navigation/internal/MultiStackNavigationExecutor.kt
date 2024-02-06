package com.hoc081098.solivagant.navigation.internal

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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
internal class MultiStackNavigationExecutor(
  private val stack: MultiStack,
  private val viewModel: StoreViewModel,
  private val onRootChanged: (NavRoot) -> Unit,
) : NavigationExecutor {
  private val _lifecycleOwner = MutableStateFlow<LifecycleOwner?>(null)

  val visibleEntries: State<ImmutableList<StackEntry<*>>>
    get() = stack.visibleEntries

  val canNavigateBack: State<Boolean>
    get() = stack.canNavigateBack

  init {
    viewModel.viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
      _lifecycleOwner
        .flatMapLatest { it?.lifecycle?.eventFlow ?: emptyFlow() }
        .collect(stack::handleLifecycleEvent)
    }
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

  override fun stackEntryIdFor(route: BaseRoute): StackEntryId = entryFor(route).id

  @Deprecated("Should not use destinationId directly, use route instead.")
  override fun stackEntryIdFor(destinationId: DestinationId<*>): StackEntryId = entryFor(destinationId).id

  override fun savedStateHandleFor(id: StackEntryId): SavedStateHandle {
    val entry = entryFor<BaseRoute>(id)
    return viewModel.provideSavedStateHandle(entry.id, entry.route)
  }

  override fun savedStateHandleFactoryFor(id: StackEntryId): SavedStateHandleFactory {
    val entry = entryFor<BaseRoute>(id)
    return viewModel.provideSavedStateHandleFactory(entry.id, entry.route)
  }

  override fun storeFor(id: StackEntryId): NavigationExecutor.Store {
    val entry = entryFor<BaseRoute>(id)
    return viewModel.provideStore(entry.id)
  }

  override fun extra(id: StackEntryId): Serializable {
    val entry = entryFor<BaseRoute>(id)
    return entry.destination.extra!!
  }

  //region Find StackEntry by id or route or destinationId
  private fun <T : BaseRoute> entryFor(id: StackEntryId): StackEntry<T> {
    return stack.entryFor(id)
      ?: error("Route with id=$id not found on back stack")
  }

  private fun <T : BaseRoute> entryFor(route: T): StackEntry<T> {
    return stack.entryFor(route)
      ?: error("Route $route not found on back stack")
  }

  private fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T> {
    return stack.entryFor(destinationId)
      ?: error("Route with destinationId=$destinationId not found on back stack")
  }
  //endregion

  fun setLifecycleOwner(lifecycleOwner: LifecycleOwner?) {
    _lifecycleOwner.value = lifecycleOwner
  }
}
