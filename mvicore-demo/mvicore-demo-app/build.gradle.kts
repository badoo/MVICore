plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("mvi-core-lint")
    id("mvi-core-detekt")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.badoo.mvicoredemo"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // MVICore
    implementation(project(":mvicore"))
    implementation(project(":mvicore-android"))
    implementation(project(":mvicore-debugdrawer"))
    implementation(project(":mvicore-demo:mvicore-demo-feature1"))
    implementation(project(":mvicore-demo:mvicore-demo-feature2"))
    implementation(project(":mvicore-plugin:middleware"))

    // Kotlin
    implementation(libs.kotlin.stdlib)

    // Android
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)

    // Rx
    implementation(libs.rxjava2)
    implementation(libs.rxandroid)

    // DI
    implementation(libs.dagger.runtime)
    implementation(libs.dagger.android.runtime)
    implementation(libs.dagger.android.support)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.android.processor)

    // DebugDrawer
    debugImplementation(libs.debugdrawer.impl)
    debugImplementation(libs.debugdrawer.view.impl)
    releaseImplementation(libs.debugdrawer.noop)
    releaseImplementation(libs.debugdrawer.view.noop)
    implementation(libs.debugdrawer.commons)
    implementation(libs.debugdrawer.actions)
    implementation(libs.debugdrawer.scalpel)
    implementation(libs.debugdrawer.base)
    implementation(libs.debugdrawer.timber)
    implementation(libs.debugdrawer.networkQuality)

    // Utils
    implementation(libs.timber)
    implementation(libs.scalpel)
    implementation(libs.okhttp)
    implementation(libs.glide.runtime)
    kapt(libs.glide.compiler)
}
