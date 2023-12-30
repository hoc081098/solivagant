package com.freeletics.khonshu.navigation

import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.khonshu.navigation.internal.DelegatingOnBackPressedCallback
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.NavEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.ActivityResultEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.BackEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.BackToEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.MultiNavEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.NavigateToActivityEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.NavigateToEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.UpEvent
import com.freeletics.khonshu.navigation.internal.NavEventCollector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow

public interface Navigator {
    /**
     * Triggers navigation to the given [route].
     */
    public fun navigateTo(route: NavRoute)

    /**
     * Triggers navigation to the given [root]. The current back stack will be removed
     * and saved. Whether the backstack of the given `root` is restored depends on
     * [restoreRootState].
     */
    public fun navigateToRoot(
        root: NavRoot,
        restoreRootState: Boolean = false,
    )

    /**
     * Triggers navigation to the given [route].
     */
    public fun navigateTo(route: ActivityRoute)

    /**
     * Triggers up navigation.
     */
    public fun navigateUp()

    /**
     * Removes the top entry from the backstack to show the previous destination.
     */
    public fun navigateBack()

    /**
     * Removes all entries from the backstack until [T]. If [inclusive] is
     * `true` [T] itself will also be removed.
     */
    @InternalNavigationApi
    public fun <T : BaseRoute> navigateBackToInternal(popUpTo: DestinationId<T>, inclusive: Boolean = false)

    /**
     * Reset the back stack to the given [root]. The current back stack will cleared and if
     * root was already on it it will be recreated.
     */
    public fun resetToRoot(root: NavRoot)

    /**
     * Replace the current back stack with the given [root].
     * The current back stack will cleared and the given [root] will be recreated.
     * After this call the back stack will only contain the given [root].
     *
     * This differs from [resetToRoot] in that [resetToRoot] does not pop the start route (exclusive)
     * whereas this does.
     */
    public fun replaceAll(root: NavRoot)

    public companion object {
        /**
         * Removes all entries from the backstack until [T]. If [inclusive] is
         * `true` [T] itself will also be removed.
         */
        public inline fun <reified T : NavRoute> Navigator.navigateBackTo(inclusive: Boolean = false) {
            navigateBackToInternal(DestinationId(T::class), inclusive)
        }
    }
}

public interface ResultNavigator {
    /**
     * Delivers the [result] to the destination that created [key].
     */
    public fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O)
}

public interface ActivityResultNavigator {
    /**
     * Launches the given [request].
     */
    public fun navigateForResult(request: ActivityResultRequest<Void?, *>)

    /**
     * Launches the given [request] with the given [input].
     */
    public fun <I> navigateForResult(request: ActivityResultRequest<I, *>, input: I)

    /**
     * Launches the [request] for the given [permissions].
     *
     * Compared to using [navigateForResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     */
    public fun requestPermissions(request: PermissionsResultRequest, vararg permissions: String)

    /**
     * Launches the [request] for the given [permissions].
     *
     * Compared to using [navigateForResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     */
    public fun requestPermissions(request: PermissionsResultRequest, permissions: List<String>)
}

public interface BackInterceptor {
    /**
     * Returns a [Flow] that will emit [Unit] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    public fun backPresses(): Flow<Unit>

    /**
     * Returns a [Flow] that will emit [value] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    public fun <T> backPresses(value: T): Flow<T>
}

/**
 * This allows to trigger navigation actions from outside the view layer
 * without keeping references to Android framework classes that might leak. It also improves
 * the testability of your navigation logic since it is possible to just write test that
 * the correct events were emitted.
 *
 * For back press handling based on logic [backPresses] is available. Activity results and
 * permission requests can be handled through [registerForActivityResult]/[navigateForResult]
 * and [registerForPermissionsResult]/[requestPermissions] respectively.
 */
public open class NavEventNavigator : Navigator, ResultNavigator, ActivityResultNavigator, BackInterceptor {

    private val _navEvents = Channel<NavEvent>(Channel.UNLIMITED)

    @InternalNavigationApi
    public val navEvents: Flow<NavEvent> = _navEvents.receiveAsFlow()

    private val _activityResultRequests = mutableListOf<ContractResultOwner<*, *, *>>()
    private val _navigationResultRequests = mutableListOf<NavigationResultRequest<*>>()
    private val _onBackPressedCallback = DelegatingOnBackPressedCallback()

    /**
     * Register for receiving activity results for the given [contract].
     *
     * The returned [ActivityResultRequest] can be used to collect incoming results (via
     * [ActivityResultRequest.results]) and to launch the actual activity result call via
     * [NavEventNavigator.navigateForResult].
     *
     * For permission requests prefer using [registerForPermissionsResult] instead.
     *
     * Note: You must call this before [NavigationSetup] is called with this navigator."
     */
    protected fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
    ): ActivityResultRequest<I, O> {
        checkAllowedToAddRequests()
        val request = ActivityResultRequest(contract)
        _activityResultRequests.add(request)
        return request
    }

    /**
     * Register for receiving permission results.
     *
     * The returned [PermissionsResultRequest] can be used to collect incoming permission results (via
     * [PermissionsResultRequest.results]) and to launch the actual permission result call via
     * [NavEventNavigator.requestPermissions].
     *
     * Compared to using [registerForActivityResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     *
     * Note: You must call this before [NavigationSetup] is called with this navigator."
     */
    protected fun registerForPermissionsResult(): PermissionsResultRequest {
        checkAllowedToAddRequests()
        val request = PermissionsResultRequest()
        _activityResultRequests.add(request)
        return request
    }

    /**
     * Register for receiving navigation results that were delivered through
     * [deliverNavigationResult]. [T] is expected to be the [BaseRoute] to the current destination.
     *
     * The returned [NavigationResultRequest] has a [NavigationResultRequest.Key]. This `key` should
     * be passed to the target destination which can then use it to call [deliverNavigationResult].
     *
     * Note: You must call this before [NavigationSetup] is called with this navigator."
     */
    protected inline fun <reified T : BaseRoute, reified O : Parcelable> registerForNavigationResult():
        NavigationResultRequest<O> {
        return registerForNavigationResult(DestinationId(T::class), O::class.qualifiedName!!)
    }

    @PublishedApi
    internal fun <T : BaseRoute, O : Parcelable> registerForNavigationResult(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        checkAllowedToAddRequests()
        val requestKey = "${id.route.qualifiedName!!}-$resultType"
        val key = NavigationResultRequest.Key<O>(id, requestKey)
        val request = NavigationResultRequest(key)
        _navigationResultRequests.add(request)
        return request
    }

    /**
     * Triggers a new [NavEvent] to navigate to the given [route].
     */
    override fun navigateTo(route: NavRoute) {
        val event = NavigateToEvent(route)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] to navigate to the given [root]. The current back stack will
     * be popped and saved. Whether the backstack of the given `root` is restored depends on
     * [restoreRootState].
     */
    override fun navigateToRoot(
        root: NavRoot,
        restoreRootState: Boolean,
    ) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] to navigate to the given [route].
     */
    override fun navigateTo(route: ActivityRoute) {
        val event = NavigateToActivityEvent(route)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that causes up navigation.
     */
    override fun navigateUp() {
        val event = UpEvent
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that pops the back stack to the previous destination.
     */
    override fun navigateBack() {
        val event = BackEvent
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that collects and combines the nav events sent in the block so they can be
     * handled individually.
     *
     * Note: This should be used when navigating multiple times, for example calling `navigateBackTo`
     * followed by `navigateTo`.
     */
    public fun navigate(block: Navigator.() -> Unit) {
        val navEvents = NavEventCollector().apply(block).navEvents
        sendNavEvent(MultiNavEvent(navEvents))
    }

    @InternalNavigationApi
    override fun <T : BaseRoute> navigateBackToInternal(popUpTo: DestinationId<T>, inclusive: Boolean) {
        val event = BackToEvent(popUpTo, inclusive)
        sendNavEvent(event)
    }

    /**
     * Reset the back stack to the given [root]. The current back stack will cleared and if
     * root was already on it it will be recreated.
     */
    override fun resetToRoot(root: NavRoot) {
        val event = NavEvent.ResetToRoot(root)
        sendNavEvent(event)
    }

    public override fun replaceAll(root: NavRoot) {
        val event = NavEvent.ReplaceAll(root)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that launches the given [request].
     *
     * The [request] can be obtained by calling [registerForActivityResult].
     */
    override fun navigateForResult(request: ActivityResultRequest<Void?, *>) {
        navigateForResult(request, null)
    }

    /**
     * Triggers a new [NavEvent] that launches the given [request] with the given [input].
     *
     * The [request] can be obtained by calling [registerForActivityResult].
     */
    override fun <I> navigateForResult(request: ActivityResultRequest<I, *>, input: I) {
        val event = ActivityResultEvent(request, input)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that requests the given [permissions].
     *
     * Compared to using [registerForActivityResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     *
     * The [request] can be obtained by calling [registerForPermissionsResult].
     */
    override fun requestPermissions(request: PermissionsResultRequest, vararg permissions: String) {
        requestPermissions(request, permissions.toList())
    }

    /**
     * Triggers a new [NavEvent] that requests the given [permissions].
     *
     * Compared to using [registerForActivityResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     *
     * The [request] can be obtained by calling [registerForPermissionsResult].
     */
    override fun requestPermissions(request: PermissionsResultRequest, permissions: List<String>) {
        val event = ActivityResultEvent(request, permissions)
        sendNavEvent(event)
    }

    /**
     * Delivers the [result] to the destination that created [key].
     */
    override fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        val event = NavEvent.DestinationResultEvent(key, result)
        sendNavEvent(event)
    }

    private fun sendNavEvent(event: NavEvent) {
        val result = _navEvents.trySendBlocking(event)
        check(result.isSuccess)
    }

    /**
     * Returns a [Flow] that will emit [Unit] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    override fun backPresses(): Flow<Unit> {
        return backPresses(Unit)
    }

    /**
     * Returns a [Flow] that will emit [value] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    override fun <T> backPresses(value: T): Flow<T> {
        return callbackFlow {
            val onBackPressed = {
                check(trySendBlocking(value).isSuccess)
            }

            _onBackPressedCallback.addCallback(onBackPressed)

            awaitClose {
                _onBackPressedCallback.removeCallback(onBackPressed)
            }
        }
    }

    private var allowedToAddRequests = true

    private fun checkAllowedToAddRequests() {
        check(allowedToAddRequests) {
            "Failed to register for result! You must call this before NavigationSetup is called with this navigator."
        }
    }

    @InternalNavigationApi
    public val activityResultRequests: List<ContractResultOwner<*, *, *>>
        get() {
            allowedToAddRequests = false
            return _activityResultRequests.toList()
        }

    @InternalNavigationApi
    public val navigationResultRequests: List<NavigationResultRequest<*>>
        get() {
            allowedToAddRequests = false
            return _navigationResultRequests.toList()
        }

    @InternalNavigationApi
    public val onBackPressedCallback: OnBackPressedCallback get() = _onBackPressedCallback
}
