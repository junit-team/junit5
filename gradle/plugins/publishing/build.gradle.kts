import junitbuild.extensions.markerCoordinates
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21

plugins {
	`kotlin-dsl`
}

dependencies {
	implementation("junitbuild.base:dsl-extensions")
	implementation(libs.plugins.jreleaser.markerCoordinates)
	constraints {
		implementation("org.eclipse.jgit:org.eclipse.jgit") {
			version {
				require("6.10.1.202505221210-r")
			}
			because("Workaround for CVE-2025-4949")
		}
		implementation("org.apache.commons:commons-lang3") {
			version {
				require("3.18.0")
			}
			because("Workaround for CVE-2025-48924")
		}
	}
}

tasks.compileJava {
	options.release = 21
}

kotlin {
	compilerOptions {
		jvmTarget = JVM_21
		freeCompilerArgs.add("-Xjdk-release=21")
	}
}
