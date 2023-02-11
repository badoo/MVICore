plugins {
    id("java")
    id("kotlin")
    id("org.jetbrains.intellij") version "1.8.0"
    id("idea")
    id("mvi-core-detekt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

group = "com.github.badoo.mvicore"
base.archivesBaseName = "mvicore-plugin-idea"
version = "0.0.2"

// Required as the "intellij" plugin is overriding the repositories from "settings.gradle"
repositories {
    google()
    mavenCentral()
}

// TODO: Publish new version - https://github.com/badoo/MVICore/issues/170
intellij {
    pluginName.set("mvicore-plugin")
    version.set("2021.2.1")
    plugins.set(listOf("android"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("145.0")
    }
}

dependencies {
    implementation(libs.rxjava2)
    implementation(libs.rxkotlin)
    implementation(libs.gson)
    implementation(project(":mvicore-plugin:common"))
    implementation(libs.kotlin.stdlib)
}
