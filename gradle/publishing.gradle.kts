apply(plugin = "de.marcphilipp.nexus-publish")
apply(plugin = "signing")

val isSnapshot = project.version.toString().contains("SNAPSHOT")
val isContinuousIntegrationEnvironment = System.getenv("CI")?.toBoolean() ?: false
val isJitPackEnvironment = System.getenv("JITPACK")?.toBoolean() ?: false

// ensure project is built successfully before publishing it
val build = tasks[LifecycleBasePlugin.BUILD_TASK_NAME]
tasks[PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME].dependsOn(build)
tasks[MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME].dependsOn(build)

configure<SigningExtension> {
	sign(the<PublishingExtension>().publications)
	setRequired(!(isSnapshot || isContinuousIntegrationEnvironment || isJitPackEnvironment))
}

configure<PublishingExtension> {
	publications {
		create<MavenPublication>("maven") {
			pom {
				name.set(provider {
					project.description ?: "${project.group}:${project.name}"
				})
				url.set("https://junit.org/junit5/")
				scm {
					connection.set("scm:git:git://github.com/junit-team/junit5.git")
					developerConnection.set("scm:git:git://github.com/junit-team/junit5.git")
					url.set("https://github.com/junit-team/junit5")
				}
				licenses {
					license {
						val license: License by rootProject.extra
						name.set(license.name)
						url.set(license.url.toString())
					}
				}
				developers {
					developer {
						id.set("bechte")
						name.set("Stefan Bechtold")
						email.set("stefan.bechtold@me.com")
					}
					developer {
						id.set("jlink")
						name.set("Johannes Link")
						email.set("business@johanneslink.net")
					}
					developer {
						id.set("marcphilipp")
						name.set("Marc Philipp")
						email.set("mail@marcphilipp.de")
					}
					developer {
						id.set("mmerdes")
						name.set("Matthias Merdes")
						email.set("Matthias.Merdes@heidelberg-mobil.com")
					}
					developer {
						id.set("sbrannen")
						name.set("Sam Brannen")
						email.set("sam@sambrannen.com")
					}
					developer {
						id.set("sormuras")
						name.set("Christian Stein")
						email.set("sormuras@gmail.com")
					}
				}
			}
		}
	}
}
