plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.detekt)
}

dependencies {
    implementation(libs.plugin.android)
    implementation(libs.plugin.kotlin)
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
