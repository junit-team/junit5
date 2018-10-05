import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("com.github.johnrengelman.shadow")
}

apply(from = "$rootDir/gradle/testing.gradle")

description = "JUnit Jupiter Params"

dependencies {
	api(project(":junit-jupiter-api"))

	shadowed("com.univocity:univocity-parsers:${Versions.univocity}")

	testImplementation(project(":junit-platform-testkit"))
	testImplementation(project(":junit-jupiter-engine"))
	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))

	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
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
	relocate("com.univocity", "org.junit.jupiter.params.shadow.com.univocity")
	from(projectDir) {
		include("LICENSE-univocity-parsers.md")
		into("META-INF")
	}
}

afterEvaluate {
	// Use classes modified by shadow plugin for test execution so JaCoCo
	// finds same class files when creating its report
	tasks.named<Test>("test") {
		classpath.minus(sourceSets["main"].output)
		classpath.plus(files(shadowJar.archivePath))
		dependsOn(shadowJar)
	}
}

tasks.named<Jar>("jar") {
	enabled = false
	dependsOn(shadowJar)
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.jupiter.params"
		)
	}
}
