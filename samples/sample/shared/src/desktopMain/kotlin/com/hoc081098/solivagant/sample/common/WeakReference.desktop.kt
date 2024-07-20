package com.hoc081098.solivagant.sample.common

// TODO: https://youtrack.jetbrains.com/issue/KT-37316
// TODO: https://youtrack.jetbrains.com/issue/KT-68674. Target versions: 2.0.20-Beta1, 2.0.10
@Suppress(
  "ACTUAL_WITHOUT_EXPECT", // internal expect is not matched with internal typealias to public type
  "NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS", // False positive ACTUAL_WITHOUT_EXPECT in K2
)
internal actual typealias WeakReference<T> = java.lang.ref.WeakReference<T>
