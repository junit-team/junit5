import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	jacoco
}

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

	val jacocoClassesDir: File by rootProject.extra

	val jar by tasks.existing(Jar::class)

	val extractJar by tasks.registering(Copy::class) {
		from(zipTree(jar.map { it.archiveFile }))
		into(jacocoClassesDir)
		include("**/*.class")
		// don't version-specific classes of MR JARs
		exclude("META-INF/versions/**")
		includeEmptyDirs = false
		onlyIf { jar.get().enabled }
	}

	jar {
		finalizedBy(extractJar)
	}

	tasks.named<JacocoReport>("jacocoTestReport") {
		enabled = false
	}

	pluginManager.withPlugin("com.github.johnrengelman.shadow") {

		val shadowJar by tasks.existing(ShadowJar::class) {
			finalizedBy(extractJar)
		}
		extractJar {
			from(zipTree(shadowJar.map { it.archiveFile }))
			// don't report coverage for shadowed classes
			exclude("**/shadow/**")
			onlyIf { shadowJar.get().enabled }
		}
	}
}
