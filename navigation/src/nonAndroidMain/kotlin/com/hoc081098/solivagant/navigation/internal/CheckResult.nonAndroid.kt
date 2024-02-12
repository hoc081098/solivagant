package com.hoc081098.solivagant.navigation.internal

/**
 * Denotes that the annotated method returns a result that it typically is an error to ignore. This
 * is usually used for methods that have no side effect, so calling it without actually looking at
 * the result usually means the developer has misunderstood what the method does.
 *
 * Example:
 * ```
 * public @CheckResult String trim(String s) { return s.trim(); }
 * ...
 * s.trim(); // this is probably an error
 * s = s.trim(); // ok
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
)
internal actual annotation class CheckResult actual constructor(actual val suggest: String)
