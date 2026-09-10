pluginManagement {
  repositories {
    google()
    maven { url = java.net.URI("https://maven-central.storage-download.googleapis.com/maven2/") }
    gradlePluginPortal()
    mavenCentral()
  }
}

// plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    maven { url = java.net.URI("https://maven-central.storage-download.googleapis.com/maven2/") }
    mavenCentral()
  }
}

rootProject.name = "RunicStave"

include(":app")
