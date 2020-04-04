import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import kotlin.reflect.KProperty

class Versions(private val project: Project) {

    companion object {
        val jvmTarget = JavaVersion.VERSION_1_8
    }

    operator fun get(name: String): String =
            project.extra.get("$name.version") as String

}
