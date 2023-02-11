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
        minSdk = 15
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

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.java8)
    implementation(libs.kotlin.stdlib)

    implementation(libs.rxjava2)
    implementation(libs.rxkotlin)
    implementation(libs.rxandroid)

    testImplementation(libs.junit5)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.platform.launcher)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)

    implementation(project(":mvicore"))
}
