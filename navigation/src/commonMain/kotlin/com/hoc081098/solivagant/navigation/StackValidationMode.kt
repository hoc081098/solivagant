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
