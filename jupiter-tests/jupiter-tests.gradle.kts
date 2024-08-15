import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	id("junitbuild.code-generator")
	id("junitbuild.kotlin-library-conventions")
	id("junitbuild.junit4-compatibility")
	id("junitbuild.testing-conventions")
	groovy
}

dependencies {
	testImplementation(projects.junitJupiter)
	testImplementation(projects.junitJupiterMigrationsupport)
	testImplementation(projects.junitPlatformLauncher)
	testImplementation(projects.junitPlatformSuiteEngine)
	testImplementation(projects.junitPlatformTestkit)
	testImplementation(testFixtures(projects.junitPlatformCommons))
	testImplementation(kotlin("stdlib"))
	testImplementation(libs.jimfs)
	testImplementation(libs.junit4)
	testImplementation(libs.kotlinx.coroutines)
	testImplementation(libs.groovy4)
	testImplementation(libs.memoryfilesystem)
	testImplementation(testFixtures(projects.junitJupiterApi))
	testImplementation(testFixtures(projects.junitJupiterEngine))
}

tasks {
	test {
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
		systemProperty("developmentVersion", version)
	}
	test_4_12 {
		filter {
			includeTestsMatching("org.junit.jupiter.migrationsupport.*")
		}
	}
}
