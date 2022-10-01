plugins {
	id("io.spring.nohttp")
	id("io.github.gradle-nexus.publish-plugin")
	`base-conventions`
	`build-metadata`
	`dependency-update-check`
	`jacoco-conventions`
	`jacoco-report-aggregation`
	`temp-maven-repo`
}

description = "JUnit 5"

val license by extra(License(
	name = "Eclipse Public License v2.0",
	url = uri("https://www.eclipse.org/legal/epl-v20.html"),
	headerFile = file("src/spotless/eclipse-public-license-2.0.java")
))

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

nexusPublishing {
	packageGroup.set("org.junit")
	repositories {
		sonatype()
	}
}

nohttp {
	source.exclude("buildSrc/build/generated-sources/**")
}

dependencies {
	(modularProjects + listOf(projects.platformTests.dependencyProject)).forEach {
		jacocoAggregation(project(it.path))
	}
}

reporting {
	reports {
		create<JacocoCoverageReport>("jacocoRootReport") {
			testType.set(TestSuiteType.UNIT_TEST)
			afterEvaluate {
				reportTask.configure {
					classDirectories.setFrom(
						files(
							classDirectories.files
								.filter { it.exists() }
								.map {
									zipTree(it).matching {
										// Use MR-JAR classes instead
										exclude("org/junit/platform/console/options/ConsoleUtils.class")
										exclude("org/junit/platform/commons/util/ModuleUtils.class")
										// Exclude shadowed classes
										exclude("**/shadow/**") // exclude MR-JAR classes
									}
								}
						)
					)
				}
			}
		}
	}
}
