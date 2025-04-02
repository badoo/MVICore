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
    namespace = "com.badoo.mvicore.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 15
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
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(project(":mvicore"))
    api(project(":binder"))
    api(libs.androidx.lifecycle.common)
    api(libs.rxjava2)

    implementation(libs.kotlin.stdlib)
    implementation(libs.rxandroid)

    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testImplementation(libs.androidx.arch.core.runtime)
    testImplementation(libs.androidx.lifecycle.runtime)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hamcrest.core)
}
