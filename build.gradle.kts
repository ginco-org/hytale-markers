import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.3.10"
    id("com.gradleup.shadow") version "9.3.2"
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.vanniktech.maven.publish") version "0.30.0"
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


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name = "hytale-markers"
        description = "General-purpose location marker storage for Hytale plugin development"
        url = "https://github.com/ginco-org/hytale-markers"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "ginco"
                name = "GINCo"
                url = "https://ginco.gg"
            }
        }
        scm {
            connection = "scm:git:git://github.com/ginco-org/hytale-markers.git"
            developerConnection = "scm:git:ssh://github.com/ginco-org/hytale-markers.git"
            url = "https://github.com/ginco-org/hytale-markers"
        }
    }
}

dependencies {
    val server_version: String by project
    compileOnly("com.hypixel.hytale:Server:${server_version}")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("dev.jonrapp:hytale-reactiveui:1.0")

    implementation("gg.ginco:hytale-codec-annotations:1.0.6")
    implementation("gg.ginco:hytale-codec-runtime:1.0.6")
    ksp("gg.ginco:hytale-codec-processor:1.0.6")
}
