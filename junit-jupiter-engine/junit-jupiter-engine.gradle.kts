import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	`kotlin-library-conventions`
	`testing-conventions`
	groovy
}

description = "JUnit Jupiter Engine"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(projects.junitPlatformEngine)
	api(projects.junitJupiterApi)

	testImplementation(projects.junitPlatformLauncher)
	testImplementation(projects.junitPlatformRunner)
	testImplementation(projects.junitPlatformTestkit)
	testImplementation(testFixtures(projects.junitPlatformCommons))
	testImplementation(kotlin("stdlib"))
	testImplementation(libs.kotlinx.coroutines)
	testImplementation(libs.groovy3)
}

tasks {
	test {
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
	}
}
