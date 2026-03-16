plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "hytale-markers"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.hytale.com/release") }
    }
}