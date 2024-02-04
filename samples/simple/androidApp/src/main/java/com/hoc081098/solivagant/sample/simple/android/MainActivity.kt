package com.hoc081098.solivagant.sample.simple.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hoc081098.solivagant.sample.simple.SimpleSolivagantSampleApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { SimpleSolivagantSampleApp() }
  }
}
