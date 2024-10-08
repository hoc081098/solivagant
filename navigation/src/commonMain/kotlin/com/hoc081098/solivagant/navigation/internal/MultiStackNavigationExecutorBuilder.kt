/*
 * Copyright 2021 Freeletics GmbH.
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.hoc081098.kmp.viewmodel.compose.kmpViewModel
import com.hoc081098.kmp.viewmodel.createSavedStateHandle
import com.hoc081098.kmp.viewmodel.viewModelFactory
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.navigation.ContentDestination
import com.hoc081098.solivagant.navigation.NavDestination
import com.hoc081098.solivagant.navigation.NavRoot
import com.hoc081098.solivagant.navigation.StackValidationMode
import kotlinx.collections.immutable.ImmutableSet

@Composable
internal fun rememberNavigationExecutor(
  startRoot: NavRoot,
  destinations: ImmutableSet<NavDestination>,
  stackValidationMode: StackValidationMode,
  viewModel: StoreViewModel = kmpViewModel(
    viewModelFactory {
      StoreViewModel(
        globalSavedStateHandle = createSavedStateHandle(),
      )
    },
  ),
): MultiStackNavigationExecutor {
  val lifecycleOwner = LocalLifecycleOwner.current

  val executor = viewModel.getMultiStackNavigationExecutor(
    startRoot = startRoot,
    contentDestinations = remember(destinations) { destinations.filterIsInstance<ContentDestination<*>>() },
    lifecycleOwner = lifecycleOwner,
    stackValidationMode = stackValidationMode,
  )

  DisposableEffect(executor, lifecycleOwner) {
    // Setup the executor with proper LifecycleOwner
    executor.setLifecycleOwner(lifecycleOwner)
    onDispose { executor.setLifecycleOwner(null) }
  }

  return executor
}
