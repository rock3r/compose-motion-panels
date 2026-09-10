import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    kotlin("multiplatform") version "2.3.20" apply false
    kotlin("jvm") version "2.3.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20" apply false
    id("org.jetbrains.compose") version "1.12.0" apply false
    id("com.android.kotlin.multiplatform.library") version "9.1.1" apply false
    id("com.android.application") version "9.1.1" apply false
    id("com.vanniktech.maven.publish") version "0.37.0" apply false
}

group = "io.github.rock3r"
version = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0-SNAPSHOT")

subprojects {
    group = rootProject.group
    version = rootProject.version

    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()

            pom {
                name.set(project.name)
                description.set(project.description)
                inceptionYear.set("2026")
                url.set("https://github.com/rock3r/compose-motion-panels")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("rock3r")
                        name.set("Sebastiano Poggi")
                        url.set("https://github.com/rock3r")
                    }
                }
                scm {
                    url.set("https://github.com/rock3r/compose-motion-panels")
                    connection.set("scm:git:git://github.com/rock3r/compose-motion-panels.git")
                    developerConnection.set("scm:git:ssh://git@github.com/rock3r/compose-motion-panels.git")
                }
            }
        }
    }
}
