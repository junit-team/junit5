import org.gradle.api.JavaVersion
import org.gradle.api.provider.Property

@Suppress("LeakingThis")
abstract class JavaLibraryExtension {

    abstract val mainJavaVersion: Property<JavaVersion>
    abstract val testJavaVersion: Property<JavaVersion>

    init {
        mainJavaVersion.convention(JavaVersion.VERSION_17)
        testJavaVersion.convention(JavaVersion.VERSION_21)
    }

}
