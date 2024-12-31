import junitbuild.extensions.capitalized

val tempRepoName by extra("temp")
val tempRepoDir by extra {
	layout.buildDirectory.dir("repo").get().asFile
}

val clearTempRepoDir by tasks.registering {
	val dir = tempRepoDir
	doFirst {
		dir.deleteRecursively()
	}
}

subprojects {
	pluginManager.withPlugin("maven-publish") {
		configure<PublishingExtension> {
			repositories {
				maven {
					name = tempRepoName
					url = uri(tempRepoDir)
				}
			}
		}
		tasks.withType<PublishToMavenRepository>()
			.named { it.endsWith("To${tempRepoName.capitalized()}Repository") }
			.configureEach {
					dependsOn(clearTempRepoDir)
			}
	}
}
