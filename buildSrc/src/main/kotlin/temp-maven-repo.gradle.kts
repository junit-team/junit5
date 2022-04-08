import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.*

val tempRepoName by extra("temp")
val tempRepoDir by extra(file("$buildDir/repo"))

val clearTempRepoDir by tasks.registering {
	doFirst {
		tempRepoDir.deleteRecursively()
	}
}

subprojects {
	pluginManager.withPlugin("maven-publish") {
		configure<PublishingExtension>() {
			repositories {
				maven {
					name = tempRepoName
					url = uri(tempRepoDir)
				}
			}
		}
		tasks.withType<PublishToMavenRepository>().configureEach {
			if (name.endsWith("To${tempRepoName.capitalize()}Repository")) {
				dependsOn(clearTempRepoDir)
			}
		}
	}
}
