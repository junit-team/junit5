import junitbuild.extensions.markerCoordinates
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21

plugins {
	`kotlin-dsl`
}

dependencies {
	implementation("junitbuild.base:dsl-extensions")
	implementation(libs.plugins.jreleaser.markerCoordinates)
	constraints {
		implementation("com.hierynomus:sshj:0.40.0") {
			because("Workaround for CVE-2020-36843")
		}
		implementation("org.eclipse.jgit:org.eclipse.jgit:7.3.0.202506031305-r") {
			because("Workaround for CVE-2025-4949")
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
