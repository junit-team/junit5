plugins {
	jacoco
}

val enableJaCoCo = project.hasProperty("enableJaCoCo")

jacoco {
	val libs = project.extensions["libs"] as VersionCatalog
	toolVersion = libs.findVersion("jacoco").get().requiredVersion
}

tasks {
	withType<Test>().configureEach {
		configure<JacocoTaskExtension> {
			enabled = enableJaCoCo
		}
	}
	withType<JacocoReport>().configureEach {
		enabled = enableJaCoCo
	}
}
