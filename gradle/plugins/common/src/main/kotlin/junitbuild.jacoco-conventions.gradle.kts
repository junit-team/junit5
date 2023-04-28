plugins {
	jacoco
	id("junitbuild.build-parameters")
}

jacoco {
	toolVersion = requiredVersionFromLibs("jacoco")
}

tasks.withType<JacocoReport>().configureEach {
	enabled = buildParameters.testing.enableJaCoCo
}
