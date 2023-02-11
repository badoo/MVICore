plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka")
    id("mvi-core-publish-android")
    id("mvi-core-lint")
    id("mvi-core-detekt")
}

group = "com.github.badoo.mvicore"

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 19
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.constraintlayout)

    implementation(libs.rxjava2)
    implementation(libs.rxkotlin)

    implementation(libs.kotlin.stdlib)

    implementation(libs.debugdrawer.base)
    debugImplementation(libs.debugdrawer.impl)
    debugImplementation(libs.debugdrawer.view.impl)
    releaseImplementation(libs.debugdrawer.noop)
    releaseImplementation(libs.debugdrawer.view.noop)

    implementation(project(":mvicore"))
}
