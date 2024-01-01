package com.hoc081098.solivagant.navigation.internal

import android.os.Parcel
import com.hoc081098.kmp.viewmodel.parcelable.Parceler
import kotlin.reflect.KClass

internal actual object KClassParceler : Parceler<KClass<*>> {
  override fun create(parcel: Parcel): KClass<*> {
    @Suppress("DEPRECATION")
    return (parcel.readSerializable() as Class<*>).kotlin
  }

  override fun KClass<*>.write(parcel: Parcel, flags: Int) = parcel.writeSerializable(this.java)
}
