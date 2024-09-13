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
    api(libs.rxjava3)
    implementation(libs.rxkotlin)
    implementation(libs.kotlin.stdlib)

    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testImplementation(libs.mockito.kotlin)
}
