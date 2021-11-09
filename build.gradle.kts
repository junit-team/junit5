plugins {
	id("io.spring.nohttp")
	id("io.github.gradle-nexus.publish-plugin")
	`base-conventions`
	`build-metadata`
	`dependency-update-check`
	`jacoco-conventions`
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

val jacocoTestProjects = listOf(
	projects.junitJupiterEngine,
	projects.junitJupiterMigrationsupport,
	projects.junitJupiterParams,
	projects.junitVintageEngine,
	projects.platformTests
).map { it.dependencyProject }
val jacocoClassesDir by extra(file("$buildDir/jacoco/classes"))

val jacocoRootReport by tasks.registering(JacocoReport::class) {
	modularProjects.forEach {
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
