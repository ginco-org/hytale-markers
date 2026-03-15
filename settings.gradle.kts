plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "hytale-markers"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.hytale.com/release") }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ginco-org/hytale-codec")
            credentials {
                username = providers.gradleProperty("githubActor").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("githubToken").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}