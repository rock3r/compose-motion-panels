import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.testing.Test

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        namespace = "dev.letstri.motionpanels.demo.shared"
        compileSdk = 36
        minSdk = 23
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        withJava()
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":panels"))
            implementation("org.jetbrains.compose.runtime:runtime:1.10.3")
            implementation("org.jetbrains.compose.foundation:foundation:1.10.3")
            implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha05")
            implementation("org.jetbrains.compose.ui:ui:1.10.3")
        }
        named("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
        }
        named("desktopTest").dependencies {
            implementation(kotlin("test"))
            implementation("org.junit.jupiter:junit-jupiter:5.14.3")
            implementation("dev.sebastiano.spectre:spectre-core:0.4.0")
            implementation("dev.sebastiano.spectre:spectre-testing:0.4.0")
        }
    }
}

tasks.named<Test>("desktopTest") {
    useJUnitPlatform()
    jvmArgs("-Djava.awt.headless=false", "-Dskiko.renderApi=SOFTWARE_COMPAT")
}

tasks.register("spectreTest") {
    group = "verification"
    description = "Runs the Spectre end-to-end desktop tests."
    dependsOn("desktopTest")
}

compose.desktop {
    application {
        mainClass = "dev.letstri.motionpanels.demo.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Compose Motion Panels"
            packageVersion = "1.0.0"
        }
    }
}
