import junitbuild.java.WriteArtifactsFile

plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.shadow-conventions")
}

description = "JUnit Platform Console Standalone"

dependencies {
	shadowed(projects.junitPlatformReporting)
	shadowed(projects.junitPlatformConsole)
	shadowed(projects.junitPlatformSuiteEngine)
	shadowed(projects.junitJupiterEngine)
	shadowed(projects.junitJupiterParams)
	shadowed(projects.junitVintageEngine)
	shadowed(libs.apiguardian) {
		because("downstream projects need it to avoid compiler warnings")
	}
}

val jupiterVersion = rootProject.version
val vintageVersion: String by project

tasks {
	jar {
		manifest {
			attributes("Main-Class" to "org.junit.platform.console.ConsoleLauncher")
		}
	}
	val shadowedArtifactsFile by registering(WriteArtifactsFile::class) {
		from(configurations.shadowed)
		outputFile = layout.buildDirectory.file("shadowed-artifacts")
	}
	shadowJar {
		// https://github.com/junit-team/junit5/issues/2557
		// exclude compiled module declarations from any source (e.g. /*, /META-INF/versions/N/*)
		exclude("**/module-info.class")
		// https://github.com/junit-team/junit5/issues/761
		// prevent duplicates, add 3rd-party licenses explicitly
		exclude("META-INF/LICENSE*.md")
		from(project.projects.junitPlatformConsole.dependencyProject.projectDir) {
			include("LICENSE-picocli.md")
			into("META-INF")
		}
		from(project.projects.junitJupiterParams.dependencyProject.projectDir) {
			include("LICENSE-univocity-parsers.md")
			into("META-INF")
		}
		from(shadowedArtifactsFile) {
			into("META-INF")
		}

		bundle {
			val importAPIGuardian: String by extra
			bnd("""
				# Customize the imports because this is an aggregate jar
				Import-Package: \
					$importAPIGuardian,\
					kotlin.*;resolution:="optional",\
					*
				# Disable the APIGuardian plugin since everything was already
				# processed, again because this is an aggregate jar
				-export-apiguardian:
			""")
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
					// See https://openjdk.java.net/jeps/238 for details
					// Note: the "jar --update ... --release X" command does not work with the
					// shadowed JAR as it contains nested classes that do not comply with multi-release jars.
					"Multi-Release" to true
			))
		}
	}

	// This jar contains some Java 9 code
	// (org.junit.platform.console.ConsoleLauncherToolProvider which implements
	// java.util.spi.ToolProvider which is @since 9).
	// So in order to resolve this, it can only run on Java 9
	osgiProperties {
		property("-runee", "JavaSE-9")
	}
}
