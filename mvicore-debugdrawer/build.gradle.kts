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
    namespace = "com.badoo.mvicore.debugdrawer"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":mvicore"))
    api(libs.debugdrawer.base)
    debugApi(libs.debugdrawer.impl)
    releaseApi(libs.debugdrawer.noop)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.rxjava2)
    implementation(libs.kotlin.stdlib)

    debugImplementation(libs.debugdrawer.view.impl)

    releaseImplementation(libs.debugdrawer.view.noop)
}
