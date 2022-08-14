import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal class AndroidMviCorePublishPlugin : MviCorePublishPlugin() {
    override fun configureDocAndSources(project: Project) {
        project.configure<LibraryExtension> {
            publishing {
                singleVariant(getComponentName()) {
                    withSourcesJar()
                    withJavadocJar()
                }
            }
        }
    }

    override fun getComponentName(): String = "release"
}
