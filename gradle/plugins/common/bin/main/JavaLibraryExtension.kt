import org.gradle.api.JavaVersion

open class JavaLibraryExtension {
    var mainJavaVersion: JavaVersion = JavaVersion.VERSION_1_8
    var testJavaVersion: JavaVersion = JavaVersion.VERSION_21
    var configureRelease: Boolean = true
}
