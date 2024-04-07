plugins {
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.badoo.mvicore"
version = "0.8"

// Required as the "intellij" plugin is overriding the repositories from "settings.gradle"
repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
}

// TODO: Publish new version - https://github.com/badoo/MVICore/issues/170
intellij {
    pluginName.set("MVICoreFileTemplates")
    version.set("2021.2.1")
    plugins.set(listOf("android"))
}

tasks {
    patchPluginXml {
        changeNotes.set(
            """
              0.8
              Fixed plugin work for Android Studio 4.1
            """
        )
        sinceBuild.set("145.0")
        untilBuild.set(null as String?)
    }
}
