@file:Suppress("ClassName")

import java.net.URL
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)

  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.parcelize)

  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.poko)

  alias(libs.plugins.vanniktech.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlinx.binary.compatibility.validator)
  alias(libs.plugins.kotlinx.kover)
}

compose {
  kotlinCompilerPlugin.set(libs.versions.jetbrains.compose.compiler)
}

kotlin {
  explicitApi()

  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.toolchain.get()))
    vendor.set(JvmVendorSpec.AZUL)
  }

  // Supports targets that have MainCoroutineDispatcher (following kmp-viewmodel artifact)
  // Also constrained by https://github.com/JetBrains/compose-multiplatform-core/blob/71ccb291b6fc5d840da05cef108ae11384bfe584/compose/ui/ui/build.gradle#L142-L150
  // Ref: https://github.com/JetBrains/compose-multiplatform-core/blob/9806d785bf33e25b0dda4853d492b319cf9a819f/buildSrc/private/src/main/kotlin/androidx/build/AndroidXComposeMultiplatformExtensionImpl.kt#L171-L176

  androidTarget {
    publishAllLibraryVariants()

    compilations.configureEach {
      compilerOptions.configure {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.target.get()))
      }
    }
  }

  jvm {
    compilations.configureEach {
      compilerOptions.configure {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.target.get()))
      }
    }
  }

  js(IR) {
    moduleName = property("POM_ARTIFACT_ID")!!.toString()
    compilations.configureEach {
      compilerOptions.configure {
        sourceMap.set(true)
        moduleKind.set(JsModuleKind.MODULE_COMMONJS)
      }
    }
    browser()
  }
  @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
  wasmJs {
    // Module name should be different from the one from JS
    // otherwise IC tasks that start clashing different modules with the same module name
    moduleName = property("POM_ARTIFACT_ID")!!.toString() + "Wasm"
    browser()
  }

  iosArm64()
  iosX64()
  iosSimulatorArm64()

  macosX64()
  macosArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      dependencies {
        api(compose.runtime)
        api(compose.runtimeSaveable)
        api(compose.foundation)
        api(compose.ui)

        api(libs.kotlinx.collections.immutable)
        api(projects.khonshuNavigationCore)

        api(libs.kmp.viewmodel.core)
        api(libs.kmp.viewmodel.savedstate)
        api(libs.kmp.viewmodel.compose)

        implementation(libs.uuid)
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation("io.kotest:kotest-assertions-core:5.9.0.1436-SNAPSHOT")
      }
    }

    val commonJvmMain by creating {
      dependsOn(commonMain.get())
    }
    val commonJvmTest by creating {
      dependsOn(commonTest.get())
    }

    val nonJvmMain by creating {
      dependsOn(commonMain.get())
    }
    val nonJvmTest by creating {
      dependsOn(commonTest.get())
    }

    androidMain {
      dependsOn(commonJvmMain)

      dependencies {
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.annotation)
      }
    }
    val androidUnitTest by getting {
      dependsOn(commonJvmTest)

      dependencies {
        implementation(kotlin("test-junit"))
      }
    }

    val nonAndroidMain by creating {
      dependsOn(commonMain.get())
    }
    val nonAndroidTest by creating {
      dependsOn(commonTest.get())
    }

    jvmMain {
      dependsOn(nonAndroidMain)
      dependsOn(commonJvmMain)
    }
    jvmTest {
      dependsOn(nonAndroidTest)
      dependsOn(commonJvmTest)

      dependencies {
        implementation(kotlin("test-junit"))
      }
    }

    // We use a common folder instead of a common source set because there is no commonizer
    // which exposes the browser APIs across these two targets.
    jsMain {
      kotlin.srcDir("src/browserMain/kotlin")

      dependsOn(nonAndroidMain)
      dependsOn(nonJvmMain)
    }
    jsTest {
      dependsOn(nonAndroidTest)
      dependsOn(nonJvmTest)

      dependencies {
        implementation(kotlin("test-js"))
      }
    }
    val wasmJsMain by getting {
      kotlin.srcDir("src/browserMain/kotlin")

      dependsOn(nonAndroidMain)
      dependsOn(nonJvmMain)
    }
    val wasmJsTest by getting {
      dependsOn(nonAndroidTest)
      dependsOn(nonJvmTest)

      dependencies {
        implementation(kotlin("test-wasm-js"))
      }
    }

    nativeMain {
      dependsOn(nonAndroidMain)
      dependsOn(nonJvmMain)
    }
    nativeTest {
      dependsOn(nonAndroidTest)
      dependsOn(nonJvmTest)
    }

    val iosAndTvOsMain by creating {
      dependsOn(appleMain.get())
    }
    val iosAndTvOsTest by creating {
      dependsOn(appleTest.get())
    }

    iosMain {
      dependsOn(iosAndTvOsMain)
    }
    iosTest {
      dependsOn(iosAndTvOsTest)
    }

    tvosMain {
      dependsOn(iosAndTvOsMain)
    }
    tvosTest {
      dependsOn(iosAndTvOsTest)
    }
  }

  sourceSets.matching { it.name.contains("Test") }.all {
    languageSettings {
      optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
  }

  sourceSets.configureEach {
    languageSettings {
      optIn("com.hoc081098.solivagant.navigation.InternalNavigationApi")
    }
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
  compileSdk = libs.versions.android.compile.get().toInt()
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  namespace = "com.hoc081098.solivagant.navigation"

  defaultConfig {
    minSdk = libs.versions.android.min.get().toInt()
  }

  // still needed for Android projects despite toolchain
  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.target.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.target.get())
  }
}

mavenPublishing {
  publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01, automaticRelease = true)
  signAllPublications()
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
  dokkaSourceSets {
    configureEach {
      externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")

      // TODO: Issue https://github.com/Kotlin/dokka/issues/3368 and https://github.com/Kotlin/dokka/issues/2037
      externalDocumentationLink(
        url = "https://hoc081098.github.io/kmp-viewmodel/docs/0.x/API/",
        packageListUrl = "https://hoc081098.github.io/kmp-viewmodel/docs/0.x/API/package-list",
      )

      perPackageOption {
        // Will match all .internal packages and sub-packages, regardless of module.
        matchingRegex.set(""".*\.internal.*""")
        suppress.set(true)
      }

      sourceLink {
        localDirectory.set(projectDir.resolve("src"))
        remoteUrl.set(URL("https://github.com/hoc081098/solivagant/tree/master/navigation/src"))
        remoteLineSuffix.set("#L")
      }
    }
  }
}
