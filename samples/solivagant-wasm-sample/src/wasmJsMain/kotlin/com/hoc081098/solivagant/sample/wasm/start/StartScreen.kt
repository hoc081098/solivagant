package com.hoc081098.solivagant.sample.wasm.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hoc081098.kmp.viewmodel.compose.kmpViewModel
import com.hoc081098.kmp.viewmodel.createSavedStateHandle
import com.hoc081098.solivagant.lifecycle.LocalLifecycleOwner
import com.hoc081098.solivagant.lifecycle.compose.collectAsStateWithLifecycle
import com.hoc081098.solivagant.lifecycle.compose.currentStateAsState
import com.hoc081098.solivagant.navigation.NavEventNavigator
import com.hoc081098.solivagant.sample.wasm.LocalNavigator
import com.hoc081098.solivagant.sample.wasm.second.SecondScreenRoute
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import solivagant.samples.`solivagant-wasm-sample`.generated.resources.Res
import solivagant.samples.`solivagant-wasm-sample`.generated.resources.compose_multiplatform

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun StartScreen(
  modifier: Modifier = Modifier,
  navigator: NavEventNavigator = LocalNavigator.current,
  viewModel: StartViewModel = kmpViewModel {
    StartViewModel(savedStateHandle = createSavedStateHandle())
  },
) {
  SideEffect { println(">>> StartScreen recomposition ${viewModel.route}") }

  val timer by viewModel.timerFlow.collectAsStateWithLifecycle()
  val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().wrapContentHeight(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "Start screen",
        style = MaterialTheme.typography.headlineMedium,
      )

      Spacer(modifier = Modifier.height(16.dp))

      Image(
        modifier = Modifier.size(200.dp),
        painter = painterResource(Res.drawable.compose_multiplatform),
        contentDescription = null,
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Timer: $timer, lifecycle: $lifecycleState",
        style = MaterialTheme.typography.titleMedium,
      )

      Spacer(modifier = Modifier.height(16.dp))

      ElevatedButton(
        onClick = { navigator.navigateTo(SecondScreenRoute) },
      ) {
        Text("Go to second screen")
      }
    }
  }
}
