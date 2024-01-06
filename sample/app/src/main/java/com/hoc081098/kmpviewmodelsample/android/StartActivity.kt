package com.hoc081098.kmpviewmodelsample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hoc081098.solivagant.sample.SolivagantSampleApp

class StartActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      SolivagantSampleApp()
    }
  }
}
