import org.gradle.api.JavaVersion

@Suppress("UnstableApiUsage")
open class JavaLibraryExtension {
    var mainJavaVersion: JavaVersion = Versions.jvmTarget
    var testJavaVersion: JavaVersion = JavaVersion.VERSION_11
}
