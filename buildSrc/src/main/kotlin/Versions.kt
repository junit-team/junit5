import org.gradle.api.JavaVersion
import org.gradle.api.Project

class Versions(project: Project) {

    companion object {
        val jvmTarget = JavaVersion.VERSION_1_8
    }

    private val properties = project.rootProject.properties
            .filterKeys { it.endsWith(".version") }
            .mapKeys { it.key.substringBeforeLast(".version") }
            .mapValues { it.value.toString() }

    val `apiguardian-api` by properties
    val junit4 by properties
    val junit4Min by properties
    val opentest4j by properties
    val picocli by properties
    val `univocity-parsers` by properties

    val archunit by properties
    val assertj by properties
    val bartholdy by properties
    val classgraph by properties
    val `commons-io` by properties
    val `kotlinx-coroutines-core` by properties
    val groovy by properties
    val log4j by properties
    val mockito by properties
    val slf4j by properties

    val checkstyle by properties
    val jacoco by properties
    val jmh by properties
    val ktlint by properties
    val surefire by properties
    val bnd by properties
}
