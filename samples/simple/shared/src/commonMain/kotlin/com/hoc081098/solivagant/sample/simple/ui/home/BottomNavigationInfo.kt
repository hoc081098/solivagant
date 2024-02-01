package com.hoc081098.solivagant.sample.simple.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import com.hoc081098.solivagant.navigation.BaseRoute
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.sample.simple.ui.home.feed.FeedTabRoute
import com.hoc081098.solivagant.sample.simple.ui.home.feed.nested_feed.NestedFeedScreenRoute
import com.hoc081098.solivagant.sample.simple.ui.home.notifications.NotificationsTabRoute
import com.hoc081098.solivagant.sample.simple.ui.home.profile.ProfileTabRoute
import kotlin.reflect.KClass
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.adapters.ImmutableListAdapter
import kotlinx.collections.immutable.persistentHashMapOf

@Immutable
enum class BottomNavigationInfo {
  FEED {
    override val root get() = FeedTabRoute
    override val title: String get() = "Feed"
    override val iconVector: ImageVector get() = Icons.Filled.Home
  },
  NOTIFICATIONS {
    override val root get() = NotificationsTabRoute
    override val title: String get() = "Notifications"
    override val iconVector: ImageVector get() = Icons.Filled.Notifications
  },
  PROFILE {
    override val root get() = ProfileTabRoute
    override val title: String get() = "Profile"
    override val iconVector: ImageVector get() = Icons.Filled.AccountCircle
  }, ;

  abstract val root: NavRoot
  abstract val title: String
  abstract val iconVector: ImageVector

  companion object {
    @Stable
    val ALL: ImmutableList<BottomNavigationInfo> = ImmutableListAdapter(entries)

    @Stable
    fun fromRoute(route: BaseRoute?): BottomNavigationInfo? = route?.let { MAP[it::class] }

    @Stable
    private val MAP: ImmutableMap<KClass<out BaseRoute>, BottomNavigationInfo> by lazy {
      persistentHashMapOf(
        // Feed
        FeedTabRoute::class to FEED,
        NestedFeedScreenRoute::class to FEED,
        // Notifications
        NotificationsTabRoute::class to NOTIFICATIONS,
        // Profile
        ProfileTabRoute::class to PROFILE,
      )
    }
  }
}
