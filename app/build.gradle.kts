import dev.sanson.lightroom.buildlogic.BuildType

plugins {
    id("dev.sanson.lightroom.android.application")
    id("dev.sanson.lightroom.android.hilt")
}

android {
    namespace = "dev.sanson.lightroom"

    defaultConfig {
        applicationId = "dev.sanson.lightroom"
        versionCode = libs.versions.versioncode.get().toInt()
        versionName = libs.versions.app.get()
    }

    buildTypes {
        debug {
            applicationIdSuffix = BuildType.Debug.applicationIdSuffix
        }

        @Suppress("UnstableApiUsage")
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
