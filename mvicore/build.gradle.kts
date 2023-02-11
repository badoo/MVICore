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

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    api(project(":binder"))
    implementation(libs.rxjava2)
    implementation(libs.rxkotlin)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.junit5)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.mockito.kotlin)
}
