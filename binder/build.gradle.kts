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

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    api(libs.rxjava2)
    implementation(libs.rxkotlin)
    implementation(libs.kotlin.stdlib)

    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.api)
    testImplementation(libs.mockito.kotlin)
}
