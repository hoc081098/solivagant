@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.kotlin.cocoapods)

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

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  cocoapods {
    summary = "Some description for the Shared Module"
    homepage = "Link to the Shared Module homepage"
    version = "1.0"
    ios.deploymentTarget = "14.1"
    podfile = project.file("../iosApp/Podfile")
    framework {
      baseName = "shared"

      export(libs.kmp.viewmodel.core)
      //      export(libs.kmp.viewmodel.savedstate)
      export("io.github.hoc081098:kmp-viewmodel-savedstate:0.6.2-SNAPSHOT")

      export(libs.napier)
      export(libs.coroutines.core)
    }
  }

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
        //        api(libs.kmp.viewmodel.savedstate)
        api("io.github.hoc081098:kmp-viewmodel-savedstate:0.6.2-SNAPSHOT")
        implementation(libs.kmp.viewmodel.compose)

        implementation("io.coil-kt.coil3:coil-core:3.0.0-alpha01")
        implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha01")

        // Import coil-network and an HTTP client engine.
        implementation("io.coil-kt.coil3:coil-network:3.0.0-alpha01")

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
        implementation("io.ktor:ktor-client-okhttp:2.3.7")
      }
    }
    val androidUnitTest by getting

    val desktopMain by getting {
      dependencies {
        api(compose.preview)

        implementation(libs.coroutines.swing)
      }
    }
    val desktopTest by getting

    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    iosMain {}

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
