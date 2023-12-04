/**
 * Precompiled [junitbuild.settings-conventions.settings.gradle.kts][Junitbuild_settings_conventions_settings_gradle] script plugin.
 *
 * @see Junitbuild_settings_conventions_settings_gradle
 */
public
class Junitbuild_settingsConventionsPlugin : org.gradle.api.Plugin<org.gradle.api.initialization.Settings> {
    override fun apply(target: org.gradle.api.initialization.Settings) {
        try {
            Class
                .forName("Junitbuild_settings_conventions_settings_gradle")
                .getDeclaredConstructor(org.gradle.api.initialization.Settings::class.java, org.gradle.api.initialization.Settings::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
