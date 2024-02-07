/*
 * Copyright 2024 Petrus Nguyễn Thái Học
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
