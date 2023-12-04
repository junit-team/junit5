/**
 * Precompiled [junitbuild.testing-conventions.gradle.kts][Junitbuild_testing_conventions_gradle] script plugin.
 *
 * @see Junitbuild_testing_conventions_gradle
 */
public
class Junitbuild_testingConventionsPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Junitbuild_testing_conventions_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
