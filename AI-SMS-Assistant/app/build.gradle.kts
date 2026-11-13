plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.alireza.aismsassistant"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.alireza.aismsassistant"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0.0"
    }
}
