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
    api(project(":binder"))
    api(project(":mvicore-plugin:common"))
    api(libs.rxjava3)
    api(libs.gson)

    implementation(libs.kotlin.stdlib)
    implementation(libs.rxkotlin)
}
