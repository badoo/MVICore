plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.detekt)
}

dependencies {
    implementation(libs.plugin.android)
    implementation(libs.plugin.kotlin)
}

tasks.withType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile::class.java).configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.name
}

detekt {
    buildUponDefaultConfig = true
    config.from(file("../../detekt.yml"))
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
