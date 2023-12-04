/**
 * Precompiled [junitbuild.build-metadata.gradle.kts][Junitbuild_build_metadata_gradle] script plugin.
 *
 * @see Junitbuild_build_metadata_gradle
 */
public
class Junitbuild_buildMetadataPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Junitbuild_build_metadata_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
