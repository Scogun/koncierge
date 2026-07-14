plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.android.multiplatform.library) apply false
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    group = "com.ucasoft.koncierge"

    repositories {
        google()
        mavenCentral()
    }

    version = "0.1.0"
}