import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke

val Project.javaModuleName: String
    get() = "org." + this.name.replace('-', '.')

val Project.versions: Versions
    get() {
        val versions: Versions by rootProject.extra {
            Versions(rootProject)
        }
        return versions
    }
