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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
  /**
   * Represents the strict stack validation mode.
   * In this mode, the stack is always kept in a valid state.
   * An exception is thrown if the stack transitions to an invalid state.
   */
  @Immutable
  public data object Strict : StackValidationMode

  /**
   * Represents the lenient stack validation mode.
   * In this mode, the stack is always kept in a valid state.
   * If the stack transitions to an invalid state, no action is taken.
   */
  @Immutable
  public data object Lenient : StackValidationMode

  /**
   * Represents the warning stack validation mode.
   * In this mode, the stack is always kept in a valid state.
   * A warning is logged if the stack transitions to an invalid state.
   *
   * @property logWarn A function that logs a warning message.
   */
  @Immutable
  @Poko
  public class Warning(
    public val logWarn: (tag: String, message: String) -> Unit,
  ) : StackValidationMode {
    public companion object {
      /**
       * The log tag used for warning messages.
       */
      @Stable
      public const val LOG_TAG: String = "[solivagant]"

      /**
       * A debug instance of the Warning class that uses the default logWarn function.
       */
      @Stable
      public val Debug: Warning = Warning(logWarn = ::logWarn)
    }
  }
}

/**
 * Executes a block of code based on the stack validation mode.
 *
 * @param strictCondition A lambda that returns a boolean indicating the strict condition.
 * @param lazyMessage A lambda that returns a message string.
 * @param unsafeBlock A lambda to execute if the condition is not met in lenient or warning mode.
 * @param safeBlock A lambda to execute if the condition is met.
 * @return The result of the executed block.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <R> StackValidationMode.executeBasedOnValidationMode(
  strictCondition: () -> Boolean,
  lazyMessage: () -> String,
  unsafeBlock: () -> R,
  safeBlock: () -> R,
): R {
  contract {
    callsInPlace(strictCondition, InvocationKind.EXACTLY_ONCE)
    callsInPlace(safeBlock, InvocationKind.AT_MOST_ONCE)
    callsInPlace(unsafeBlock, InvocationKind.AT_MOST_ONCE)
  }

  val condition = strictCondition()

  return when (this) {
    Strict -> {
      check(condition, lazyMessage)
      safeBlock()
    }

    Lenient ->
      if (condition) {
        safeBlock()
      } else {
        unsafeBlock()
      }

    is Warning ->
      if (condition) {
        safeBlock()
      } else {
        logWarn(Warning.LOG_TAG, lazyMessage())
        unsafeBlock()
      }
  }
}
