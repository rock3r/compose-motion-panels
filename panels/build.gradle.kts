import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
    `maven-publish`
}

kotlin {
    android {
        namespace = "dev.letstri.motionpanels"
        compileSdk = 36
        minSdk = 23
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        withJava()
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.compose.runtime:runtime:1.10.3")
            api("org.jetbrains.compose.foundation:foundation:1.10.3")
            api("org.jetbrains.compose.ui:ui:1.10.3")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }
}

group = "io.github.rock3r"
version = "0.1.0-SNAPSHOT"
