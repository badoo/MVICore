plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("mvi-core-lint")
    id("mvi-core-detekt")
}

android {
    namespace = "com.badoo.mvicoredemo"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.badoo.mvicoredemo"
        minSdk = 21
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation(project(":binder"))

    // Kotlin
    implementation(libs.kotlin.stdlib)

    // Android
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)

    // Rx
    implementation(libs.rxjava3)
    implementation(libs.rxandroid)

    // DI
    implementation(libs.dagger.runtime)
    implementation(libs.hilt.runtime)
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    // DebugDrawer
    debugImplementation(libs.debugdrawer.impl)
    releaseImplementation(libs.debugdrawer.noop)
    implementation(libs.debugdrawer.commons)
    implementation(libs.debugdrawer.scalpel)
    implementation(libs.debugdrawer.base)
    implementation(libs.debugdrawer.timber)
    implementation(libs.debugdrawer.networkQuality)

    // Utils
    implementation(libs.timber)
    implementation(libs.scalpel)
    implementation(libs.glide.runtime)
    kapt(libs.glide.compiler)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.runner)
}
