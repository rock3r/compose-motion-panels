import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
    id("com.vanniktech.maven.publish")
}

description = "Resizable and collapsible panels for Compose Multiplatform"

kotlin {
    android {
        namespace = "dev.letstri.motionpanels"
        compileSdk = 37
        minSdk = 23
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        withJava()
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.compose.runtime:runtime:1.12.0")
            api("org.jetbrains.compose.foundation:foundation:1.12.0")
            api("org.jetbrains.compose.ui:ui:1.12.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }
}

mavenPublishing {
    coordinates(group.toString(), "compose-motion-panels", version.toString())
}
