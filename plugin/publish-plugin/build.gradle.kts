import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(libs.plugin.android)
    implementation(libs.plugin.kotlin)
}

gradlePlugin {
    plugins {
        create("mvi-core-publish-android") {
            id = "mvi-core-publish-android"
            implementationClass = "AndroidMviCorePublishPlugin"
        }
        create("mvi-core-publish-java") {
            id = "mvi-core-publish-java"
            implementationClass = "JavaMviCorePublishPlugin"
        }
    }
}
