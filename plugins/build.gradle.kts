val buildTask = tasks.register("buildPlugins")

subprojects {
    buildTask.configure { dependsOn(tasks.named("build")) }
}