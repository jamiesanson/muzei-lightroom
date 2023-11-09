@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("dev.sanson.android.library.core")
    id("dev.sanson.android.hilt")
}

android {
    namespace = "dev.sanson.lightroom.common.di"
}

dependencies {
    implementation(libs.dagger)
}
