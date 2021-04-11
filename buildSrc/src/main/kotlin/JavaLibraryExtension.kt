import org.gradle.api.JavaVersion

@Suppress("UnstableApiUsage")
open class JavaLibraryExtension {
    var mainJavaVersion: JavaVersion = JavaVersion.VERSION_1_8
    var testJavaVersion: JavaVersion = JavaVersion.VERSION_16
}
