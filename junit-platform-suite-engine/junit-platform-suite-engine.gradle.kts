plugins {
	`java-library-conventions`
	`testing-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Suite Engine"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(projects.junitPlatformEngine)
	api(projects.junitPlatformSuiteApi)

	implementation(projects.junitPlatformSuiteCommons)

	testFixturesApi(projects.junitJupiterApi)
	testFixturesApi(projects.junitPlatformSuiteApi)

	testImplementation(projects.junitPlatformTestkit)
	testImplementation(projects.junitJupiterEngine)
}
