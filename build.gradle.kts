plugins {
    kotlin("jvm") version "2.3.10"
    id("com.gradleup.shadow") version "9.3.2"
    id("com.google.devtools.ksp") version "2.3.6"
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

// kotlin-compiler-embeddable declares kotlin-reflect:1.6.10 as a runtime dependency.
// This forces the correct version matching our Kotlin plugin version.
configurations.named("kotlinCompilerClasspath") {
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-reflect:2.3.10")
}

tasks.compileJava {
    enabled = false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}


group = "gg.ginco.markers"
version = "1.0-SNAPSHOT"

dependencies {
    val server_version: String by project
    implementation("com.hypixel.hytale:Server:${server_version}")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("dev.jonrapp:hytale-reactiveui:1.0")

    implementation("gg.ginco:hytale-codec-annotations:1.0.4")
    implementation("gg.ginco:hytale-codec-runtime:1.0.4")
    ksp("gg.ginco:hytale-codec-processor:1.0.4")
}
