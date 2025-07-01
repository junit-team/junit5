import junitbuild.extensions.markerCoordinates

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
	}
}
