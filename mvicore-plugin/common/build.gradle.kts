plugins {
    id("kotlin")
    id("mvi-core-publish-java")
    id("mvi-core-detekt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

base.archivesBaseName = "mvicore-plugin-common"

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlin.stdlib)
}
