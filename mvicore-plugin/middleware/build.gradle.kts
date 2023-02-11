plugins {
    id("java")
    id("mvi-core-publish-java")
    id("kotlin")
    id("org.jetbrains.dokka")
    id("mvi-core-detekt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

base.archivesBaseName = "mvicore-plugin-middleware"

dependencies {
    implementation(libs.rxjava2)
    implementation(libs.rxkotlin)
    implementation(libs.gson)
    implementation(project(":mvicore"))
    implementation(project(":mvicore-plugin:common"))
    implementation(libs.kotlin.stdlib)
}
