package com.hoc081098.solivagant.navigation.test

import com.hoc081098.kmp.viewmodel.parcelable.Parcelable
import com.hoc081098.kmp.viewmodel.parcelable.Parcelize
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.NavRoute
import dev.drewhamilton.poko.Poko

@Poko
@Parcelize
internal class SimpleRoute(val number: Int) : NavRoute, Parcelable

@Poko
@Parcelize
internal class OtherRoute(val number: Int) : NavRoute, Parcelable

@Poko
@Parcelize
internal class ThirdRoute(val number: Int) : NavRoute, Parcelable

@Poko
@Parcelize
internal class SimpleRoot(val number: Int) : NavRoot, Parcelable

@Poko
@Parcelize
internal class OtherRoot(val number: Int) : NavRoot, Parcelable

@Poko
@Parcelize
internal class TestParcelable(val value: Int) : Parcelable
