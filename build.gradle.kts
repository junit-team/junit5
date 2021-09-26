plugins {
	id("com.github.ben-manes.versions") // gradle dependencyUpdates
	id("io.spring.nohttp")
	id("io.github.gradle-nexus.publish-plugin")
	`base-conventions`
	`build-metadata`
	`jacoco-conventions`
}

val platformProjects by extra(listOf(
		projects.junitPlatformCommons,
		projects.junitPlatformConsole,
		projects.junitPlatformConsoleStandalone,
		projects.junitPlatformEngine,
		projects.junitPlatformJfr,
		projects.junitPlatformLauncher,
		projects.junitPlatformReporting,
		projects.junitPlatformRunner,
		projects.junitPlatformSuite,
		projects.junitPlatformSuiteApi,
		projects.junitPlatformSuiteCommons,
		projects.junitPlatformSuiteEngine,
		projects.junitPlatformTestkit
).map { it.dependencyProject })

val jupiterProjects by extra(listOf(
		projects.junitJupiter,
		projects.junitJupiterApi,
		projects.junitJupiterEngine,
		projects.junitJupiterMigrationsupport,
		projects.junitJupiterParams
).map { it.dependencyProject })

val vintageProjects by extra(listOf(
		projects.junitVintageEngine.dependencyProject
))

val mavenizedProjects by extra(platformProjects + jupiterProjects + vintageProjects)
val modularProjects by extra(mavenizedProjects - listOf(projects.junitPlatformConsoleStandalone.dependencyProject))

val license by extra(License(
		name = "Eclipse Public License v2.0",
		url = uri("https://www.eclipse.org/legal/epl-v20.html"),
		headerFile = file("src/spotless/eclipse-public-license-2.0.java")
))

val tempRepoName by extra("temp")
val tempRepoDir by extra(file("$buildDir/repo"))

val jacocoTestProjects = listOf(
		projects.junitJupiterEngine,
		projects.junitJupiterMigrationsupport,
		projects.junitJupiterParams,
		projects.junitVintageEngine,
		projects.platformTests
).map { it.dependencyProject }
val jacocoCoveredProjects = modularProjects
val jacocoClassesDir by extra(file("$buildDir/jacoco/classes"))

nexusPublishing {
	packageGroup.set("org.junit")
	repositories {
		sonatype()
	}
}

val clearTempRepoDir by tasks.registering {
	doFirst {
		tempRepoDir.deleteRecursively()
	}
}

subprojects {

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

	tasks.withType<AbstractArchiveTask>().configureEach {
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true
		dirMode = Integer.parseInt("0755", 8)
		fileMode = Integer.parseInt("0644", 8)
	}

	pluginManager.withPlugin("maven-publish") {
		configure<PublishingExtension> {
			repositories {
				repositories {
					maven {
						name = tempRepoName
						url = uri(tempRepoDir)
					}
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

rootProject.apply {
	description = "JUnit 5"

	nohttp {
		// Must cast, since `source` is only exposed as a FileTree
		(source as ConfigurableFileTree).exclude("buildSrc/build/generated-sources/**")
	}

	tasks {
		dependencyUpdates {
			checkConstraints = true
			resolutionStrategy {
				componentSelection {
					all {
						val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
							.map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
							.any { it.matches(candidate.version) }
						if (rejected) {
							reject("Release candidate")
						}
					}
				}
			}
		}
	}

	val jacocoRootReport by tasks.registering(JacocoReport::class) {
		jacocoCoveredProjects.forEach {
			dependsOn(it.tasks.named("extractJar"))
			it.pluginManager.withPlugin("java") {
				sourceDirectories.from(it.the<SourceSetContainer>()["main"].allSource.srcDirs)
			}
		}
		classDirectories.from(files(jacocoClassesDir))
		reports {
			html.required.set(true)
			xml.required.set(true)
			csv.required.set(false)
		}
	}

	afterEvaluate {
		jacocoRootReport {
			jacocoTestProjects.forEach {
				executionData(it.tasks.withType<Test>().map { task -> task.the<JacocoTaskExtension>().destinationFile })
				dependsOn(it.tasks.withType<Test>())
			}
		}
	}
}
