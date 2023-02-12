plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("mvi-core-lint")
    id("mvi-core-detekt")
}

android {
    namespace = "com.badoo.feature2"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
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
    api(libs.rxjava2)
    api(project(":mvicore"))
    api(project(":mvicore-demo:mvicore-demo-catapi"))

    implementation(project(":mvicore-android"))
    implementation(libs.kotlin.stdlib)
}
