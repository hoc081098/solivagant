package com.hoc081098.kmpviewmodelsample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.hoc081098.solivagant.sample.SolivagantSampleApp
import io.github.aakira.napier.Napier

class StartActivity : ComponentActivity() {
  private var addedObserver = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      SolivagantSampleApp()
    }
  }

  override fun onResume() {
    super.onResume()

    if (!addedObserver) {
      addedObserver = true

      lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
          Napier.d(message = "🚀🚀🚀 onStateChanged $event {${lifecycle.currentState}}")
        }
      })
    }
  }
}
