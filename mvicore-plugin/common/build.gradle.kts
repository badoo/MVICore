plugins {
    id("kotlin")
    id("mvi-core-publish-java")
    id("mvi-core-detekt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

base.archivesBaseName = "mvicore-plugin-common"

dependencies {
    api(libs.gson)

    implementation(libs.kotlin.stdlib)
}
