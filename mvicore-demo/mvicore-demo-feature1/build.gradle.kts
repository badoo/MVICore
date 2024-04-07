plugins {
    id("com.android.library")
    id("kotlin-android")
    id("mvi-core-lint")
    id("mvi-core-detekt")
}

android {
    namespace = "com.badoo.feature1"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(project(":mvicore"))

    implementation(libs.kotlin.stdlib)
}
