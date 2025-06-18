import junitbuild.extensions.markerCoordinates

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
		implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.1.202505221210-r") {
			because("Workaround for CVE-2025-4949")
		}
	}
}
