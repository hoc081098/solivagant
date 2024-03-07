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

// TODO: https://youtrack.jetbrains.com/issue/KT-66230/Support-Weak-References-to-Kotlin-objects-in-WASM-JS-targets
// Original JS reference
public external class WeakRef<T : JsAny>(
  @Suppress("UnusedPrivateProperty") target: T,
) {
  /**
   * Returns the WeakRef instance's target object, or undefined if the target object has been
   * reclaimed.
   */
  public fun deref(): T?
}

internal actual class WeakReference<T : Any> actual constructor(referred: T) {
  private val ref = WeakRef(referred.toJsReference())

  actual fun get(): T? = ref.deref()?.unsafeCast<JsReference<T>>()?.get()

  actual fun clear() {
    // Nothing to do
  }
}
