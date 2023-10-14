plugins {
    @Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
    run {
        alias(libs.plugins.android.application) apply false
        alias(libs.plugins.android.library) apply false
        alias(libs.plugins.kotlin.jvm) apply false
        alias(libs.plugins.kotlin.android) apply false
        alias(libs.plugins.kotlinx.serialization) apply false
        alias(libs.plugins.ktlint) apply false
        alias(libs.plugins.hilt) apply false
        alias(libs.plugins.ksp) apply false
    }
}