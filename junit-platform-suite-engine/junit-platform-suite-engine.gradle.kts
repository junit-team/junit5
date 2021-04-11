plugins {
	`java-library-conventions`
	`testing-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Suite Engine"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.platform.engine)
	api(projects.platform.suite.api)

	implementation(projects.platform.suite.commons)

	testFixturesApi(projects.jupiter.api)
	testFixturesApi(projects.platform.suite.api)

	testImplementation(projects.platform.testkit)
	testImplementation(projects.jupiter.engine)
}
