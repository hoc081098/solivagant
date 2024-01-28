@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)

  alias(libs.plugins.kotlin.serialization)

  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.parcelize)
}

compose {
  kotlinCompilerPlugin.set(libs.versions.jetbrains.compose.compiler)
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.toolchain.get()))
    vendor.set(JvmVendorSpec.AZUL)
  }

  androidTarget {
    compilations.configureEach {
      compilerOptions.configure {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(libs.versions.java.target.get()))
      }
    }
  }

  jvm("desktop") {
    compilations.configureEach {
      compilerOptions.configure {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(libs.versions.java.target.get()))
      }
    }
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "SolivagantSampleAppShared"
      isStatic = true
    }
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.ui)
        implementation(compose.material3)
        implementation(compose.material)

        api(projects.navigation)

        api(libs.kmp.viewmodel.core)
        api(libs.kmp.viewmodel.savedstate)
        implementation(libs.kmp.viewmodel.compose)
        implementation(libs.kmp.viewmodel.koin)
        implementation(libs.kmp.viewmodel.koin.compose)

        implementation(libs.coil.core)
        implementation(libs.coil.compose)
        implementation(libs.coil.network.ktor)

        implementation(libs.ktor.client.core)

        api(libs.napier)
        api(libs.coroutines.core)
        api(libs.kotlinx.collections.immutable)

        api(libs.koin.core)
        implementation(libs.koin.compose)

        implementation(libs.kotlinx.serialization.json)
        implementation(libs.flowExt)
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    androidMain {
      dependencies {
        api(libs.koin.android)

        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.runtime.compose)

        implementation(libs.coroutines.android)
        implementation(libs.ktor.client.okhttp)
      }
    }
    val androidUnitTest by getting

    val desktopMain by getting {
      dependencies {
        api(compose.preview)

        implementation(libs.coroutines.swing)
        implementation(libs.ktor.client.java)
      }
    }
    val desktopTest by getting

    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    iosMain {
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }

    val iosX64Test by getting
    val iosArm64Test by getting
    val iosSimulatorArm64Test by getting
    iosTest {}
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
  kotlinOptions {
    // 'expect'/'actual' classes (including interfaces, objects, annotations, enums,
    // and 'actual' typealiases) are in Beta.
    // You can use -Xexpect-actual-classes flag to suppress this warning.
    // Also see: https://youtrack.jetbrains.com/issue/KT-61573
    freeCompilerArgs +=
      listOf(
        "-Xexpect-actual-classes",
      )
  }
}

android {
  namespace = "com.hoc081098.solivagant.sample"
  compileSdk = libs.versions.sample.android.compile.get().toInt()
  defaultConfig {
    minSdk = libs.versions.android.min.get().toInt()
  }
  buildFeatures {
    buildConfig = true
  }
  // still needed for Android projects despite toolchain
  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.target.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.target.get())
  }
}

workaroundForIssueKT51970()

// Workaround for https://youtrack.jetbrains.com/issue/KT-51970
fun Project.workaroundForIssueKT51970() {
  afterEvaluate {
    afterEvaluate {
      tasks.configureEach {
        if (
          name.startsWith("compile") &&
          name.endsWith("KotlinMetadata")
        ) {
          enabled = false
        }
      }
    }
  }
}

// Monitor GC performance: https://kotlinlang.org/docs/native-memory-manager.html#monitor-gc-performance
kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
  binaries.all {
    freeCompilerArgs += "-Xruntime-logs=gc=info"
  }
}
