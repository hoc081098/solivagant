package com.hoc081098.solivagant.lifecycle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.hoc081098.solivagant.lifecycle.Lifecycle
import kotlinx.coroutines.flow.StateFlow

/**
 * Collects values from the [Lifecycle.currentStateFlow] and represents its latest value via [State].
 * The [StateFlow.value] is used as an initial value. Every time there would be new value posted
 * into the [StateFlow] the returned [State] will be updated causing recomposition of every
 * [State.value] usage.
 */
@Composable
public fun Lifecycle.currentStateAsState(): State<Lifecycle.State> = currentStateFlow.collectAsState()
