import org.gradle.api.JavaVersion

object Versions {

    // Languages
    val jvmTarget = JavaVersion.VERSION_1_8
    val kotlin = "1.3.31"
    val groovy = "2.5.7"

    // Dependencies
    val apiGuardian = "1.0.0"
    val junit4 = "4.12"
    val ota4j = "1.2.0-SNAPSHOT"
    val picocli = "3.9.6"
    val univocity = "2.8.2"

    // Test Dependencies
    val archunit = "0.10.2"
    val assertJ = "3.12.2"
    val bartholdy = "0.2.3"
    val classgraph = "4.8.37"
    val commonsIo = "2.6"
    val javaCompilerScriptEngine = "0.1.2"
    val log4j = "2.11.2"
    val mockito = "2.28.2"
    val slf4j = "1.7.26"

    // Plugins
    val buildScanPlugin = "2.3"
    val gitPublishPlugin = "2.1.1"
    val jmhPlugin = "0.4.8"
    val nexusPublishPlugin = "0.2.0"
    val shadowPlugin = "4.0.1"
    val spotlessPlugin = "3.23.0"
    val versioningPlugin = "2.8.2"
    val versionsPlugin = "0.21.0"

    // Asciidoctor
    val asciidoctorDiagram = "1.5.9"
    val asciidoctorJ = "1.5.7"
    val asciidoctorPdf = "1.5.0-alpha.17"
    val asciidoctorPlugin = "1.5.8.1"
    val jruby = "9.1.17.0"

    // Tools
    val checkstyle = "8.10"
    val jacoco = "0.8.2"
    val jmh = "1.21"
    val ktlint = "0.24.0"
    val surefire = "2.22.2"

}
