buildscript {
  repositories {
    google()
    mavenCentral()
  }
}

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation("com.android.tools.build:gradle:7.2.2")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
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
