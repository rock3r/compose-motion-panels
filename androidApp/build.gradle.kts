plugins {
    id("com.android.application")
}

android {
    namespace = "dev.letstri.motionpanels.demo"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.letstri.motionpanels.demo"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":demo"))
    implementation("androidx.activity:activity-compose:1.12.2")
}
