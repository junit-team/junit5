import junitbuild.extensions.markerCoordinates
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21

plugins {
	`kotlin-dsl`
}

dependencies {
	implementation("junitbuild.base:dsl-extensions")
	implementation(libs.plugins.jreleaser.markerCoordinates)
}

configurations.configureEach {
	resolutionStrategy {
		eachDependency {
			// Workaround for CVE-2025-4949
			if (requested.name == "org.eclipse.jgit") {
				useVersion("6.10.1.202505221210-r")
			}
			// Workaround for CVE-2020-36843
			if (requested.name == "sshj") {
				useVersion("0.40.0")
			}
		}
	}
}

tasks.compileJava {
	options.release = 21
}

kotlin {
	compilerOptions.jvmTarget = JVM_21
}
