plugins {
	id("com.github.johnrengelman.shadow")
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

// TODO workaround for shadow plugin, should be fixed by 4.0.4
extra["testJavaVersion"] = JavaVersion.VERSION_1_10

description = "JUnit Jupiter Params"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-jupiter-api"))

	shadowed("com.univocity:univocity-parsers:${Versions.univocity}")

	testImplementation(project(":junit-platform-testkit"))
	testImplementation(project(":junit-jupiter-engine"))
	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))

	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks {
	shadowJar {
		// Generate shadow jar only if the underlying manifest was regenerated.
		// See https://github.com/junit-team/junit5/issues/631
		onlyIf {
			(rootProject.extra["generateManifest"] as Boolean || !archivePath.exists())
		}
		archiveClassifier.set("")
		configurations = listOf(project.configurations["shadowed"])
		exclude("META-INF/maven/**")
		relocate("com.univocity", "org.junit.jupiter.params.shadow.com.univocity")
		from(projectDir) {
			include("LICENSE-univocity-parsers.md")
			into("META-INF")
		}
	}
	test {
		// in order to run the test against the shadowJar
		classpath = classpath - sourceSets.main.get().output + files(shadowJar.get().archiveFile)
		dependsOn(shadowJar)
	}
	jar {
		enabled = false
		dependsOn(shadowJar)
		manifest {
			attributes("Automatic-Module-Name" to "org.junit.jupiter.params")
		}
	}
}
