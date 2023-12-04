/**
 * Precompiled [junitbuild.dependency-update-check.gradle.kts][Junitbuild_dependency_update_check_gradle] script plugin.
 *
 * @see Junitbuild_dependency_update_check_gradle
 */
public
class Junitbuild_dependencyUpdateCheckPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Junitbuild_dependency_update_check_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
