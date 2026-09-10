import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("com.vanniktech.maven.publish")
}

description = "Jewel and IntelliJ Islands styling for Compose Motion Panels"

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":panels"))

    // IntelliJ plugins get this from the platform. Keeping it compile-only prevents bundling a
    // second, incompatible Jewel runtime into the IDE.
    compileOnly("org.jetbrains.jewel:jewel-foundation:0.40.0-262.10315.125")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.jewel:jewel-foundation:0.40.0-262.10315.125")
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    coordinates(group.toString(), "compose-motion-panels-jewel", version.toString())
}
