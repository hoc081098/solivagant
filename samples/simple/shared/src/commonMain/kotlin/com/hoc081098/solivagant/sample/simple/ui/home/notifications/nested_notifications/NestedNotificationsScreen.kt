@file:Suppress("PackageNaming")

package com.hoc081098.solivagant.sample.simple.ui.home.notifications.nested_notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hoc081098.kmp.viewmodel.koin.compose.koinKmpViewModel
import com.hoc081098.solivagant.lifecycle.compose.collectAsStateWithLifecycle
import com.hoc081098.solivagant.sample.simple.common.debugDescription

@Composable
internal fun NestedNotificationsScreen(
  modifier: Modifier = Modifier,
  viewModel: NestedNotificationsViewModel = koinKmpViewModel(),
) {
  var savableCount by rememberSaveable { mutableIntStateOf(0) }
  val savedStateHandleCount by viewModel.countStateFlow.collectAsStateWithLifecycle()

  Surface(
    modifier = modifier.fillMaxSize(),
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = "Nested notifications: viewModel=${viewModel.debugDescription}, route=${viewModel.route}",
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "savableCount=$savableCount, savedStateHandleCount=$savedStateHandleCount",
          style = MaterialTheme.typography.titleMedium,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(
          onClick = {
            savableCount++
            viewModel.increment()
          },
        ) {
          Text(text = "Increment")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(onClick = {}) {
          Text(text = "To detail")
        }
      }
    }
  }
}
