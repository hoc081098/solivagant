package com.hoc081098.solivagant.sample.simple

import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.sample.simple.ui.home.feed.FeedTabViewModel
import com.hoc081098.solivagant.sample.simple.ui.home.feed.nested_feed.NestedFeedViewModel
import com.hoc081098.solivagant.sample.simple.ui.home.notifications.NotificationsTabViewModel
import com.hoc081098.solivagant.sample.simple.ui.home.notifications.nested_notifications.NestedNotificationsViewModel
import com.hoc081098.solivagant.sample.simple.ui.home.profile.ProfileTabViewModel
import com.hoc081098.solivagant.sample.simple.ui.login.LoginViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private val CommonModule = module {
  singleOf(::NavEventNavigator)

  factoryOf(::LoginViewModel)

  factoryOf(::FeedTabViewModel)
  factoryOf(::NestedFeedViewModel)

  factoryOf(::NotificationsTabViewModel)
  factoryOf(::NestedNotificationsViewModel)

  factoryOf(::ProfileTabViewModel)
}

expect fun setupNapier()

expect fun isDebug(): Boolean

fun startKoinCommon(
  appDeclaration: KoinAppDeclaration = {},
) {
  startKoin {
    appDeclaration()
    modules(CommonModule)
  }
}
