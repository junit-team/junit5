/**
 * Precompiled [junitbuild.temp-maven-repo.gradle.kts][Junitbuild_temp_maven_repo_gradle] script plugin.
 *
 * @see Junitbuild_temp_maven_repo_gradle
 */
public
class Junitbuild_tempMavenRepoPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Junitbuild_temp_maven_repo_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
