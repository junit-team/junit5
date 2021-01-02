plugins {
	`java-library-conventions`
	`testing-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Suite Engine"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-engine"))
	api(project(":junit-platform-suite-api"))
	api(project(":junit-platform-launcher"))

	testFixturesApi(project(":junit-jupiter-api"))
	testFixturesApi(project(":junit-platform-suite-api"))

	testImplementation(project(":junit-platform-testkit"))
	testImplementation(project(":junit-jupiter-engine"))
}
