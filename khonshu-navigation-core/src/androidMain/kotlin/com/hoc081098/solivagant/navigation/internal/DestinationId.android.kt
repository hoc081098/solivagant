package com.hoc081098.solivagant.navigation.internal

import android.os.Parcel
import com.hoc081098.kmp.viewmodel.parcelable.Parceler
import com.hoc081098.solivagant.navigation.BaseRoute

@OptIn(InternalNavigationApi::class)
internal actual object DestinationIdParceler : Parceler<DestinationId<*>> {
  override fun create(parcel: Parcel): DestinationId<*> {
    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    val kClass = (parcel.readSerializable() as Class<out BaseRoute>).kotlin
    return DestinationId(kClass)
  }

  override fun DestinationId<*>.write(parcel: Parcel, flags: Int) = parcel.writeSerializable(route.java)
}
