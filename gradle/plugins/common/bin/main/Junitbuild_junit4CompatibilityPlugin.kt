/**
 * Precompiled [junitbuild.junit4-compatibility.gradle.kts][Junitbuild_junit4_compatibility_gradle] script plugin.
 *
 * @see Junitbuild_junit4_compatibility_gradle
 */
public
class Junitbuild_junit4CompatibilityPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Junitbuild_junit4_compatibility_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
