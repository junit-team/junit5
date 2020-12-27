plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite (Aggregator)"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api(project(":junit-platform-suite-api"))
	api(project(":junit-platform-suite-engine"))
}
