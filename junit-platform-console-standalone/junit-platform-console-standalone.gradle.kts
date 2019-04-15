plugins {
	`java-library-conventions`
	id("com.github.johnrengelman.shadow")
}

description = "JUnit Platform Console Standalone"

dependencies {
	shadowed(project(":junit-platform-reporting"))
	shadowed(project(":junit-platform-console"))
	shadowed(project(":junit-jupiter-engine"))
	shadowed(project(":junit-jupiter-params"))
	shadowed(project(":junit-vintage-engine"))
}

val jupiterVersion = rootProject.version
val vintageVersion = project.properties["vintageVersion"]

tasks {
	jar {
		enabled = false
		manifest {
			attributes("Main-Class" to "org.junit.platform.console.ConsoleLauncher")
		}
		dependsOn(shadowJar)
	}
	shadowJar {
		classifier = ""
		configurations = listOf(project.configurations["shadowed"])

		// https://github.com/junit-team/junit5/issues/761
		// prevent duplicates, add 3rd-party licenses explicitly
		exclude("META-INF/LICENSE*.md")
		from(project(":junit-platform-console").projectDir) {
			include("LICENSE-picocli.md")
			into("META-INF")
		}
		from(project(":junit-jupiter-params").projectDir) {
			include("LICENSE-univocity-parsers.md")
			into("META-INF")
		}

		mergeServiceFiles()
		manifest.apply {
			inheritFrom(jar.get().manifest)
			attributes(mapOf(
					"Specification-Title" to project.name,
					"Implementation-Title" to project.name,
					// Generate test engine version information in single shared manifest file.
					// Pattern of key and value: `"Engine-Version-{YourTestEngine#getId()}": "47.11"`
					"Engine-Version-junit-jupiter" to jupiterVersion,
					"Engine-Version-junit-vintage" to vintageVersion,
					// Version-aware binaries are already included - set Multi-Release flag here.
					// See http://openjdk.java.net/jeps/238 for details
					// Note: the "jar --update ... --release X" command does not work with the
					// shadowed JAR as it contains nested classes that do comply multi-release jars.
					"Multi-Release" to true
			))
		}
	}
}
