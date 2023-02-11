pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

enableFeaturePreview("VERSION_CATALOGS")

include(
    ":binder",
    ":mvicore",
    ":mvicore-diff",
    ":mvicore-android",
    ":mvicore-debugdrawer",
    ":mvicore-plugin:middleware",
    ":mvicore-plugin:idea",
    ":mvicore-plugin:common",
    ":mvicore-plugin:templates",
    ":mvicore-demo:mvicore-demo-catapi",
    ":mvicore-demo:mvicore-demo-feature1",
    ":mvicore-demo:mvicore-demo-feature2",
    ":mvicore-demo:mvicore-demo-app",
)

includeBuild("plugins")
