import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("com.github.johnrengelman.shadow")
}

description = "JUnit Platform Console"

dependencies {
	api(project(":junit-platform-reporting"))

	shadowed("info.picocli:picocli:${Versions.picocli}")
}

val shadowJar by tasks.getting(ShadowJar::class) {
	// Generate shadow jar only if the underlying manifest was regenerated.
	// See https://github.com/junit-team/junit5/issues/631
	onlyIf {
		(rootProject.extra["generateManifest"] as Boolean || !archivePath.exists())
	}
	classifier = ""
	configurations = listOf(project.configurations["shadowed"])
	exclude("META-INF/maven/**")
	relocate("picocli", "org.junit.platform.console.shadow.picocli")
	from(projectDir) {
		include("LICENSE-picocli.md")
		into("META-INF")
	}
}

tasks.named<Jar>("jar") {
	enabled = false
	dependsOn(shadowJar)
	manifest {
		attributes(
			"Main-Class" to "org.junit.platform.console.ConsoleLauncher",
			"Automatic-Module-Name" to "org.junit.platform.console"
		)
	}
}
