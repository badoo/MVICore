import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

internal abstract class MviCorePublishPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply {
            apply("maven-publish")
        }

        configureDocAndSources(project)

        project.afterEvaluate {
            project.configure<PublishingExtension> {
                configurePublishing(project)
            }
        }
    }

    protected abstract fun configureDocAndSources(project: Project)

    protected abstract fun getComponentName(): String

    private fun PublishingExtension.configurePublishing(project: Project) {
        publications {
            setupPublications(project)
        }
    }

    private fun PublicationContainer.setupPublications(project: Project) {
        create<MavenPublication>("mviCoreRelease") {
            from(project.components[getComponentName()])
            groupId = "com.github.badoo.mvicore"
            version = if (project.hasProperty("VERSION")) {
                project.property("VERSION").toString()
            } else {
                "undefined"
            }

            pom {
                name.set("MVICore")
                description.set("MVICore")
                url.set("https://github.com/badoo/MVICore")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("badoo")
                        name.set("Badoo")
                        email.set("mvicore@team.bumble.com")
                    }
                }
                scm {
                    connection.set("scm:git:ssh://github.com/badoo/MVICore.git")
                    developerConnection.set("scm:git:ssh://github.com/badoo/MVICore.git")
                    url.set("https://github.com/badoo/MVICore")
                }
            }
        }
    }
}
