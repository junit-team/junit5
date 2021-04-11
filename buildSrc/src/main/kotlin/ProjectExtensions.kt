import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate

val Project.javaModuleName: String
    get() = "org." + this.name.replace('-', '.')

val Project.projects: Projects
    get() {
        var versions: Projects? by rootProject.extra
        return versions ?: Projects(rootProject).also { versions = it }
    }
