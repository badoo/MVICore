plugins {
    id("com.android.library")
    id("kotlin-android")
    id("mvi-core-lint")
    id("mvi-core-detekt")
}

android {
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
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)

    implementation(libs.rxjava2)
    implementation(libs.rxandroid)

    implementation(project(":mvicore"))
}
