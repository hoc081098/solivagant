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

// TODO: https://youtrack.jetbrains.com/issue/KT-37316
// TODO: https://youtrack.jetbrains.com/issue/KT-68674. Target versions: 2.0.20-Beta1, 2.0.10
@Suppress(
  "ACTUAL_WITHOUT_EXPECT", // internal expect is not matched with internal typealias to public type
  "NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS", // False positive ACTUAL_WITHOUT_EXPECT in K2
)
internal actual typealias VisibleForTesting = androidx.annotation.VisibleForTesting
