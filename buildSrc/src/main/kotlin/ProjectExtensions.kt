import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

val Project.javaModuleName: String
    get() = "org." + this.name.replace('-', '.')

val Project.versions: Versions
    get() {
        val extra = rootProject.extra
        if (!extra.has("versions")) {
            extra.set("versions", Versions(rootProject))
        }
        return extra.get("versions") as Versions
    }
