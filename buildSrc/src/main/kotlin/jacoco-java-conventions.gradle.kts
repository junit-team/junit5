import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	`java-library`
	id("jacoco-base-conventions")
}

val jacocoClassesDir: File by rootProject.extra

val extractJar by tasks.registering(Copy::class) {
	from(zipTree(tasks.jar.map { it.archiveFile }))
	into(jacocoClassesDir)
	include("**/*.class")
	// don't version-specific classes of MR JARs
	exclude("META-INF/versions/**")
	includeEmptyDirs = false
	onlyIf { tasks.jar.get().enabled }
}

tasks.jar {
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
