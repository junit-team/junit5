import org.gradle.configurationcache.extensions.capitalized

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
		tasks.withType<PublishToMavenRepository>().configureEach {
			if (name.endsWith("To${tempRepoName.capitalized()}Repository")) {
				dependsOn(clearTempRepoDir)
			}
		}
	}
}
