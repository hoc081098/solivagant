@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.android.app)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.jetbrains.compose)
}

compose {
  kotlinCompilerPlugin.set(libs.versions.jetbrains.compose.compiler)
}

android {
  namespace = "com.hoc081098.solivagant.sample.simple.android"
  compileSdk = libs.versions.sample.android.compile.get().toInt()
  defaultConfig {
    applicationId = "com.hoc081098.solivagant.sample.simple.android"
    minSdk = libs.versions.android.min.get().toInt()
    targetSdk = libs.versions.sample.android.target.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  buildFeatures {
    buildConfig = true
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.target.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.target.get())
  }
  kotlinOptions {
    jvmTarget = JavaVersion.toVersion(libs.versions.java.target.get()).toString()
  }
}

dependencies {
  implementation(project(":samples:simple:shared"))
  implementation(libs.androidx.activity.compose)
  implementation(libs.koin.android)
  debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")
}
