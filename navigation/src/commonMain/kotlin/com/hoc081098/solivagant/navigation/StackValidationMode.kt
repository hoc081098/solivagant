package com.hoc081098.solivagant.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hoc081098.solivagant.navigation.internal.utils.logWarn
import dev.drewhamilton.poko.Poko

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
