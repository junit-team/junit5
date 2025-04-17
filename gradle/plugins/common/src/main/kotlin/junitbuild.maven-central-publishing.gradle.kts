
import org.jreleaser.model.Active.RELEASE
import org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage
import java.util.Properties

plugins {
	id("org.jreleaser")
	id("junitbuild.temp-maven-repo")
}

val tempRepoDir: File by extra

tasks.jreleaserDeploy {
	dependsOn("publishAllSubprojectsToTempRepository")
	outputs.upToDateWhen { false }
	doLast {
		val outputProperties = Properties()
		layout.buildDirectory.file("jreleaser/output.properties").get().asFile.inputStream().use { input ->
			outputProperties.load(input)
		}
		val deploymentId = outputProperties.getProperty("deploymentId")
		if (deploymentId != null) {
			println("Deployment ID: $deploymentId")
			println("Staging Repo URL: https://central.sonatype.com/api/v1/publisher/deployment/$deploymentId/download")
		}
	}
}

val mavenCentralUsername = providers.gradleProperty("mavenCentralUsername")
val mavenCentralPassword = providers.gradleProperty("mavenCentralPassword")

jreleaser {
	deploy {
		maven {
			mavenCentral {
				register("artifacts") {
					active = RELEASE
					url = "https://central.sonatype.com/api/v1/publisher"
					username = mavenCentralUsername
					password = mavenCentralPassword
					stagingRepository(tempRepoDir.absolutePath)
					applyMavenCentralRules = true
					sign = false
					checksums = false
					namespace = "org.junit"
					stage = providers.gradleProperty("jreleaser.mavencentral.stage")
						.map(Stage::of)
						.orElse(Stage.UPLOAD)
				}
			}
		}
	}
}

subprojects {
	pluginManager.withPlugin("maven-publish") {
		configure<PublishingExtension> {
			repositories {
				maven {
					name = "mavenCentralSnapshots"
					url = uri("https://central.sonatype.com/repository/maven-snapshots")
					credentials {
						username = mavenCentralUsername.orNull
						password = mavenCentralPassword.orNull
					}
				}
			}
		}
	}
}
