import java.time.OffsetDateTime
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
	id("net.nemerosa.versioning")
	id("com.github.ben-manes.versions") // gradle dependencyUpdates
	id("com.diffplug.spotless")
	id("io.spring.nohttp")
}

apply(from = "gradle/build-scan-user-data.gradle")

buildScan {
	if (project.hasProperty("javaHome")) {
		value("Custom Java home", project.property("javaHome") as String)
	}
}

val buildTimeAndDate by extra {

	// SOURCE_DATE_EPOCH is a UNIX timestamp for pinning build metadata against
	// in order to achieve reproducible builds
	//
	// More details - https://reproducible-builds.org/docs/source-date-epoch/

	if (System.getenv().containsKey("SOURCE_DATE_EPOCH")) {

		val sourceDateEpoch = System.getenv("SOURCE_DATE_EPOCH").toLong()

		Instant.ofEpochSecond(sourceDateEpoch).atOffset(ZoneOffset.UTC)

	} else {
		OffsetDateTime.now()
	}
}

val buildDate by extra { DateTimeFormatter.ISO_LOCAL_DATE.format(buildTimeAndDate) }
val buildTime by extra { DateTimeFormatter.ofPattern("HH:mm:ss.SSSZ").format(buildTimeAndDate) }
val buildRevision by extra { versioning.info.commit }
val builtByValue by extra { project.findProperty("builtBy") ?: project.property("defaultBuiltBy") }

val platformProjects by extra(listOf(
		project(":junit-platform-commons"),
		project(":junit-platform-console"),
		project(":junit-platform-console-standalone"),
		project(":junit-platform-engine"),
		project(":junit-platform-jfr"),
		project(":junit-platform-launcher"),
		project(":junit-platform-reporting"),
		project(":junit-platform-runner"),
		project(":junit-platform-suite-api"),
		project(":junit-platform-testkit")
))

val jupiterProjects by extra(listOf(
		project(":junit-jupiter"),
		project(":junit-jupiter-api"),
		project(":junit-jupiter-engine"),
		project(":junit-jupiter-migrationsupport"),
		project(":junit-jupiter-params")
))

val vintageProjects by extra(listOf(
		project(":junit-vintage-engine")
))

val mavenizedProjects by extra(platformProjects + jupiterProjects + vintageProjects)
val modularProjects by extra(mavenizedProjects - listOf(project(":junit-platform-console-standalone")))

val license by extra(License(
		name = "Eclipse Public License v2.0",
		url = uri("https://www.eclipse.org/legal/epl-v20.html"),
		headerFile = file("src/spotless/eclipse-public-license-2.0.java")
))

val tempRepoName by extra("temp")
val tempRepoDir by extra(file("$buildDir/repo"))

val enableJaCoCo = project.hasProperty("enableJaCoCo")
val jacocoTestProjects = listOf(
		project(":junit-jupiter-engine"),
		project(":junit-jupiter-migrationsupport"),
		project(":junit-jupiter-params"),
		project(":junit-platform-runner"),
		project(":junit-vintage-engine"),
		project(":platform-tests")
)
val jacocoCoveredProjects = modularProjects
val jacocoClassesDir = file("$buildDir/jacoco/classes")

allprojects {

	apply(plugin = "eclipse")
	apply(plugin = "idea")
	apply(plugin = "com.diffplug.spotless")

	if (enableJaCoCo) {
		apply(plugin = "jacoco")
		configure<JacocoPluginExtension> {
			toolVersion = versions["jacoco"]
		}
	}

	repositories {
		// mavenLocal()
		mavenCentral()
		maven(url = "https://oss.sonatype.org/content/repositories/snapshots") {
			mavenContent {
				snapshotsOnly()
			}
		}
	}
}

subprojects {

	if (project in jupiterProjects) {
		group = property("jupiterGroup")!!
	}
	else if (project in platformProjects) {
		group = property("platformGroup")!!
		version = property("platformVersion")!!
	}
	else if (project in vintageProjects) {
		group = property("vintageGroup")!!
		version = property("vintageVersion")!!
	}

	tasks.withType<AbstractArchiveTask>().configureEach {
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true
		dirMode = Integer.parseInt("0755", 8)
		fileMode = Integer.parseInt("0644", 8)
	}

	pluginManager.withPlugin("java") {

		spotless {
			val headerFile = license.headerFile
			val importOrderConfigFile = rootProject.file("src/eclipse/junit-eclipse.importorder")
			val javaFormatterConfigFile = rootProject.file("src/eclipse/junit-eclipse-formatter-settings.xml")

			java {
				licenseHeaderFile(headerFile, "(package|import|open|module) ")
				importOrderFile(importOrderConfigFile)
				eclipse().configFile(javaFormatterConfigFile)
				removeUnusedImports()
				trimTrailingWhitespace()
				endWithNewline()
			}

			kotlin {
				ktlint(versions["ktlint"])
				licenseHeaderFile(headerFile)
				trimTrailingWhitespace()
				endWithNewline()
			}
		}

		afterEvaluate {
			if (enableJaCoCo && project in jacocoCoveredProjects) {
				val jarTask = (tasks.findByName("shadowJar") ?: tasks["jar"]) as Jar
				val extractJar by tasks.registering(Copy::class) {
					from(zipTree(jarTask.archivePath))
					into(jacocoClassesDir)
					include("**/*.class")
					// don't report coverage for shadowed classes
					exclude("**/shadow/**")
					// don't version-specific classes of MR JARs
					exclude("META-INF/versions/**")
					includeEmptyDirs = false
					onlyIf { jarTask.enabled }
				}
				jarTask.finalizedBy(extractJar)
			}
		}
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
	}
}

rootProject.apply {
	description = "JUnit 5"

	spotless {
		format("misc") {
			target("**/*.gradle", "**/*.gradle.kts", "**/*.gitignore")
			targetExclude("**/build/**")
			indentWithTabs()
			trimTrailingWhitespace()
			endWithNewline()
		}
		format("documentation") {
			target("**/*.adoc", "**/*.md")
			trimTrailingWhitespace()
			endWithNewline()
		}
	}

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

	if (enableJaCoCo) {
		tasks {
			val jacocoMerge by registering(JacocoMerge::class) {
				subprojects.filter { it in jacocoTestProjects }
						.forEach { subproj ->
							executionData(fileTree("dir" to "${subproj.buildDir}/jacoco", "include" to "*.exec"))
							dependsOn(subproj.tasks.withType<Test>())
						}
			}
			register<JacocoReport>("jacocoRootReport") {
				dependsOn(jacocoMerge)
				jacocoCoveredProjects.forEach {
					it.pluginManager.withPlugin("java") {
						sourceDirectories.from(it.the<SourceSetContainer>()["main"].allSource.srcDirs)
					}
				}
				classDirectories.from(files(jacocoClassesDir))
				executionData(jacocoMerge.get().destinationFile)
				reports {
					html.isEnabled = true
					xml.isEnabled = true
					csv.isEnabled = false
				}
			}
		}
	}
}
