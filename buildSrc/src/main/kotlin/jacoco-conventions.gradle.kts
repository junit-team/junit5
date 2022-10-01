import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	jacoco
}

val enableJaCoCo = project.hasProperty("enableJaCoCo")

jacoco {
	toolVersion = requiredVersionFromLibs("jacoco")
}

tasks {
	withType<Test>().configureEach {
		configure<JacocoTaskExtension> {
			isEnabled = enableJaCoCo
		}
	}
	withType<JacocoReport>().configureEach {
		enabled = enableJaCoCo
	}
}
