import org.gradle.api.Project
import kotlin.reflect.KProperty

class Projects(private val rootProject: Project) {
    val bom = project("junit-bom")
    val platform = Platform()
    val jupiter = Jupiter()
    val vintage = Vintage()

    private fun project(name: String) = rootProject.project(":${name}")

    inner class Platform {
        private val platformProject = object {
            operator fun getValue(receiver: Any?, property: KProperty<*>) = project("junit-platform-${property.name}")
        }
        val commons by platformProject
        val console by platformProject
        val `console-standalone` by platformProject
        val engine by platformProject
        val jfr by platformProject
        val launcher by platformProject
        val reporting by platformProject
        val runner by platformProject
        val suite = Suite()
        val testkit by platformProject
        val tests = project("platform-tests")

        inner class Suite {
            private val suiteProject = object {
                operator fun getValue(receiver: Any?, property: KProperty<*>) = project("junit-platform-suite-${property.name}")
            }
            val aggregator = project("junit-platform-suite")
            val api by suiteProject
            val commons by suiteProject
            val engine by suiteProject
        }
    }

    inner class Jupiter {
        private val jupiterProject = object {
            operator fun getValue(receiver: Any?, property: KProperty<*>) = project("junit-jupiter-${property.name}")
        }
        val aggregator = project("junit-jupiter")
        val api by jupiterProject
        val engine by jupiterProject
        val migrationsupport by jupiterProject
        val params by jupiterProject
    }

    inner class Vintage {
        val engine = project("junit-vintage-engine")
    }
}
