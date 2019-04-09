import org.gradle.api.JavaVersion

@Suppress("UnstableApiUsage")
open class JavaLibraryExtension {
    var automaticModuleName: String? = null
    var mainJavaVersion: JavaVersion = Versions.jvmTarget
    var nineJavaVersion: JavaVersion = JavaVersion.VERSION_1_9
    var testJavaVersion: JavaVersion = JavaVersion.VERSION_11
}
