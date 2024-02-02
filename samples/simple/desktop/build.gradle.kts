import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain by getting

    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      @OptIn(ExperimentalComposeLibrary::class)
      implementation(compose.components.resources)

      implementation(projects.samples.simple.shared)
    }

    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
    }
  }
}

compose {
  kotlinCompilerPlugin.set(libs.versions.jetbrains.compose.compiler)
}

compose.desktop {
  application {
    mainClass = "MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.hoc081098.solivagant.sample.simple"
      packageVersion = "1.0.0"
    }
  }
}
