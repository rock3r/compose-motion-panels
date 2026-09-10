pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "compose-motion-panels"
include(":panels", ":panels-jewel", ":panels-jewel-standalone", ":demo", ":androidApp")
