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
        compileSdk = 37
        minSdk = 23
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        withJava()
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":panels"))
            implementation("org.jetbrains.compose.runtime:runtime:1.12.0")
            implementation("org.jetbrains.compose.foundation:foundation:1.12.0")
            implementation("org.jetbrains.compose.material3:material3:1.12.0-alpha03")
            implementation("org.jetbrains.compose.ui:ui:1.12.0")
        }
        named("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
        }
        named("desktopTest").dependencies {
            implementation(kotlin("test"))
            implementation(project(":panels-jewel-standalone"))
            implementation("org.junit.jupiter:junit-jupiter:5.14.3")
            implementation("dev.sebastiano.spectre:spectre-core:0.4.0")
            implementation("dev.sebastiano.spectre:spectre-testing:0.4.0")
            implementation("dev.sebastiano.spectre:spectre-recording:0.4.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.11.0")
            runtimeOnly("dev.sebastiano.spectre:spectre-recording-linux:0.4.0")
            runtimeOnly("dev.sebastiano.spectre:spectre-recording-macos:0.4.0")
            runtimeOnly("dev.sebastiano.spectre:spectre-recording-windows:0.4.0")
        }
    }
}

tasks.named<Test>("desktopTest") {
    useJUnitPlatform()
    jvmArgs("-Djava.awt.headless=false", "-Dskiko.renderApi=SOFTWARE_COMPAT")
    // Xvfb provides a display but not a desktop portal. An inherited desktop session bus can make
    // Compose 1.12's synchronous system-theme lookup wait for that absent portal indefinitely.
    environment("DBUS_SESSION_BUS_ADDRESS", "")
    environment("XDG_RUNTIME_DIR", "")
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
