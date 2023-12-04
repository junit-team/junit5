plugins {
	`maven-publish`
	signing
	id("junitbuild.base-conventions")
	id("junitbuild.build-parameters")
}

val isSnapshot = project.version.toString().contains("SNAPSHOT")

val jupiterProjects: List<Project> by rootProject
val platformProjects: List<Project> by rootProject
val vintageProjects: List<Project> by rootProject

when (project) {
	in jupiterProjects -> {
		group = property("jupiterGroup")!!
	}
	in platformProjects -> {
		group = property("platformGroup")!!
		version = property("platformVersion")!!
	}
	in vintageProjects -> {
		group = property("vintageGroup")!!
		version = property("vintageVersion")!!
	}
}

// ensure project is built successfully before publishing it
tasks.withType<PublishToMavenRepository>().configureEach {
	dependsOn(provider {
		val tempRepoName: String by rootProject
		if (repository.name != tempRepoName) {
			listOf(tasks.build)
		} else {
			emptyList()
		}
	})
}
tasks.withType<PublishToMavenLocal>().configureEach {
	dependsOn(tasks.build)
}

val signArtifacts = buildParameters.publishing.signArtifacts.getOrElse(!(isSnapshot || buildParameters.ci))

signing {
	useGpgCmd()
	sign(publishing.publications)
	isRequired = signArtifacts
}

tasks.withType<Sign>().configureEach {
	enabled = signArtifacts
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			pom {
				name.set(provider {
					project.description ?: "${project.group}:${project.name}"
				})
				url = "https://junit.org/junit5/"
				scm {
					connection = "scm:git:git://github.com/junit-team/junit5.git"
					developerConnection = "scm:git:git://github.com/junit-team/junit5.git"
					url = "https://github.com/junit-team/junit5"
				}
				licenses {
					license {
						val license: License by rootProject.extra
						name = license.name
						url = license.url.toString()
					}
				}
				developers {
					developer {
						id = "bechte"
						name = "Stefan Bechtold"
						email = "stefan.bechtold@me.com"
					}
					developer {
						id = "jlink"
						name = "Johannes Link"
						email = "business@johanneslink.net"
					}
					developer {
						id = "marcphilipp"
						name = "Marc Philipp"
						email = "mail@marcphilipp.de"
					}
					developer {
						id = "mmerdes"
						name = "Matthias Merdes"
						email = "matthias.merdes@heidelpay.com"
					}
					developer {
						id = "sbrannen"
						name = "Sam Brannen"
						email = "sam@sambrannen.com"
					}
					developer {
						id = "sormuras"
						name = "Christian Stein"
						email = "sormuras@gmail.com"
					}
					developer {
						id = "juliette-derancourt"
						name = "Juliette de Rancourt"
						email = "derancourt.juliette@gmail.com"
					}
				}
			}
		}
	}
}
