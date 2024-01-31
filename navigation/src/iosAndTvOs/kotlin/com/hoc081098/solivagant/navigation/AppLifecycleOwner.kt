package com.hoc081098.solivagant.navigation

import com.hoc081098.solivagant.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.navigation.internal.AppLifecycleOwnerImpl

@Suppress("FunctionName") // Factory function
public fun AppLifecycleOwner(): LifecycleOwner = AppLifecycleOwnerImpl()
