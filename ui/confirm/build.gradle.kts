// Copyright (C) 2023, Jamie Sanson
// SPDX-License-Identifier: Apache-2.0
plugins {
    id("dev.sanson.android.feature")
}

android {
    namespace = "dev.sanson.lightroom.ui.confirm"
}

dependencies {
    implementation(projects.common.config)

    implementation(projects.lib.lightroom)
    implementation(projects.lib.lightroomCoil)
    implementation(projects.lib.muzei)

    implementation(libs.coil)
    implementation(libs.muzei.api)
}
