plugins {
	jacoco
}

val enableJaCoCo = project.hasProperty("enableJaCoCo")

jacoco {
	toolVersion = requiredVersionFromLibs("jacoco")
}

tasks.withType<JacocoReport>().configureEach {
	enabled = enableJaCoCo
}
