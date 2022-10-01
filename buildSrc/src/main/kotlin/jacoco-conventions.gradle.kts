import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.junit.gradle.jacoco.JacocoConventions.COVERAGE_CLASSES

plugins {
	jacoco
}

val mavenizedProjects: List<Project> by rootProject.extra
val enableJaCoCo = project.hasProperty("enableJaCoCo")

jacoco {
	toolVersion = requiredVersionFromLibs("jacoco")
}

tasks {
	withType<Test>().configureEach {
		configure<JacocoTaskExtension> {
			isEnabled = enableJaCoCo
		}
	}
	withType<JacocoReport>().configureEach {
		enabled = enableJaCoCo
	}
}

pluginManager.withPlugin("java") {

	val codeCoverageClassesJar by tasks.registering(Jar::class) {
		from(tasks.named<Jar>("jar").map { zipTree(it.archiveFile) })
		archiveClassifier.set("jacoco")
		enabled = project in mavenizedProjects
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
	}

	configurations.create("codeCoverageReportClasses") {
		isCanBeResolved = false
		isCanBeConsumed = true
		isTransitive = false
		attributes {
			attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class, COVERAGE_CLASSES))
		}
		outgoing.artifact(codeCoverageClassesJar)
	}
}
