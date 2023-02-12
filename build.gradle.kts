// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.plugin.android)
        classpath(libs.plugin.kotlin)
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.kotlinVersion.get()}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("mvi-core-collect-sarif")
    id("com.autonomousapps.dependency-analysis") version libs.versions.dependencyAnalysis.get()
}

dependencyAnalysis {
    issues {
        all {
            onIncorrectConfiguration {
                severity("fail")
            }
            onUnusedDependencies {
                severity("fail")
            }
        }
        project(":mvicore-demo:mvicore-demo-app") {
            onUnusedDependencies {
                severity("fail")
                exclude("com.jakewharton.scalpel:scalpel") // Accessed using reflection
            }
        }
        project(":mvicore-android") {
            onUnusedDependencies {
                severity("fail")
                exclude("androidx.test:runner") // Accessed using reflection
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
