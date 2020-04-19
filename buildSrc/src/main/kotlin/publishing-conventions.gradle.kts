import java.time.Duration

plugins {
	`maven-publish`
	signing
	id("de.marcphilipp.nexus-publish")
}

val isSnapshot = project.version.toString().contains("SNAPSHOT")
val isContinuousIntegrationEnvironment = System.getenv("CI")?.toBoolean() ?: false
val isJitPackEnvironment = System.getenv("JITPACK")?.toBoolean() ?: false

// ensure project is built successfully before publishing it
tasks.withType<PublishToMavenRepository>().configureEach {
	dependsOn(tasks.build)
}
tasks.withType<PublishToMavenLocal>().configureEach {
	dependsOn(tasks.build)
}

signing {
	sign(publishing.publications)
	isRequired = !(isSnapshot || isContinuousIntegrationEnvironment || isJitPackEnvironment)
}

tasks.withType<Sign>().configureEach {
	onlyIf {
		!isSnapshot // Gradle Module Metadata currently does not support signing snapshots
	}
}

nexusPublishing {
	connectTimeout.set(Duration.ofMinutes(2))
	clientTimeout.set(Duration.ofMinutes(2))
	packageGroup.set("org.junit")
	repositories {
		sonatype()
	}
}

publishing {
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
						email.set("matthias.merdes@heidelpay.com")
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
					developer {
						id.set("juliette-derancourt")
						name.set("Juliette de Rancourt")
						email.set("derancourt.juliette@gmail.com")
					}
				}
			}
		}
	}
}
