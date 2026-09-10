plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

description = "Standalone Jewel runtime for Compose Motion Panels"

dependencies {
    api(project(":panels-jewel"))
    api("org.jetbrains.jewel:jewel-int-ui-standalone:0.40.0-262.10315.125")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

mavenPublishing {
    coordinates(group.toString(), "compose-motion-panels-jewel-standalone", version.toString())
}
