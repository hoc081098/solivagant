enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
  repositories {
    google()
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    gradlePluginPortal()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}

rootProject.name = "solivagant"

include(":khonshu-navigation-core")
include(":lifecycle")
include(":navigation")

include(
  ":samples:sample:app",
  ":samples:sample:shared",
  ":samples:sample:desktop",
)

include(":obsolete:navigation-core")

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}
