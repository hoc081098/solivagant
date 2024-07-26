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

package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hoc081098.solivagant.navigation.StackValidationMode.Lenient
import com.hoc081098.solivagant.navigation.StackValidationMode.Strict
import com.hoc081098.solivagant.navigation.StackValidationMode.Warning
import com.hoc081098.solivagant.navigation.internal.utils.logWarn
import dev.drewhamilton.poko.Poko

/**
 * Stack validation mode.
 * - [Strict]: always keep the stack in a valid state.
 *   Throw an exception if the stack is transitioning to an invalid state.
 * - [Lenient]: always keep the stack in a valid state.
 *   Do nothing if the stack is in an invalid state (keep the stack as is).
 * - [Warning]: always keep the stack in a valid state.
 *   Log a warning if the stack is in an invalid state (keep the stack as is).
 */
@Immutable
public sealed interface StackValidationMode {
  @Immutable
  public data object Strict : StackValidationMode

  @Immutable
  public data object Lenient : StackValidationMode

  @Immutable
  @Poko
  public class Warning(
    public val logWarn: (tag: String, message: String) -> Unit,
  ) : StackValidationMode {
    public companion object {
      @Stable
      public const val LOG_TAG: String = "[solivagant]"

      @Stable
      public val Debug: Warning = Warning(logWarn = ::logWarn)
    }
  }
}
