import org.gradle.api.Project

val Project.javaModuleName: String
    get() = "org." + this.name.replace('-', '.')
