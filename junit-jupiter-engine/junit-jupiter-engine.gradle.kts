import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	`kotlin-library-conventions`
	`testing-conventions`
	groovy
}

description = "JUnit Jupiter Engine"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.platform.engine)
	api(projects.jupiter.api)

	testImplementation(projects.platform.launcher)
	testImplementation(projects.platform.runner)
	testImplementation(projects.platform.testkit)
	testImplementation(testFixtures(projects.platform.commons))
	testImplementation(kotlin("stdlib"))
	testImplementation(libs.kotlinx.coroutines)
	testImplementation(libs.groovy3)
}

tasks {
	test {
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
	}
}
