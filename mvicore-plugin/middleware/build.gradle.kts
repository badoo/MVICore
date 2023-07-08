plugins {
    id("java")
    id("mvi-core-publish-java")
    id("kotlin")
    id("org.jetbrains.dokka")
    id("mvi-core-detekt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

base.archivesBaseName = "mvicore-plugin-middleware"

dependencies {
    api(project(":binder"))
    api(project(":mvicore-plugin:common"))
    api(libs.rxjava2)
    api(libs.gson)

    implementation(libs.kotlin.stdlib)
    implementation(libs.rxkotlin)
}
