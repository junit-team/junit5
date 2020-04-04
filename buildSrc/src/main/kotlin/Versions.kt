import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import kotlin.reflect.KProperty

class Versions(private val project: Project) {

    companion object {
        val jvmTarget = JavaVersion.VERSION_1_8
    }

    private val properties = object {
        operator fun getValue(receiver: Any?, property: KProperty<*>) = get(property.name)
    }

    val junit4 by properties
    val junit4Min by properties
    val opentest4j by properties
    val apiguardian by properties
    val assertj by properties

    operator fun get(name: String) = project.extra.get("$name.version") as String

}
