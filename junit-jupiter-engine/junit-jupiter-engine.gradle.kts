import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	`kotlin-library-conventions`
	`testing-conventions`
	groovy
}

description = "JUnit Jupiter Engine"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api(libs.apiguardian)
	api(project(":junit-platform-engine"))
	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	testImplementation(testFixtures(project(":junit-platform-commons")))
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	testImplementation("org.codehaus.groovy:groovy")
}

tasks {
	test {
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
	}
}
