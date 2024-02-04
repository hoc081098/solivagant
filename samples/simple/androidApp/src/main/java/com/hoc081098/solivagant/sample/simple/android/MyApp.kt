package com.hoc081098.solivagant.sample.simple.android

import android.app.Application
import com.hoc081098.solivagant.sample.simple.setupNapier
import com.hoc081098.solivagant.sample.simple.startKoinCommon
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class MyApp : Application() {
  override fun onCreate() {
    super.onCreate()

    setupNapier()
    startKoinCommon {
      androidContext(this@MyApp)
      androidLogger(
        if (BuildConfig.DEBUG) {
          Level.DEBUG
        } else {
          Level.ERROR
        },
      )
    }
  }
}
