import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate

val Project.javaModuleName: String
    get() = "org." + this.name.replace('-', '.')
