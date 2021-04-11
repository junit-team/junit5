plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite (Aggregator)"

dependencies {
	api(platform(projects.bom))
	api(projects.platform.suite.api)
	implementation(projects.platform.suite.engine)
}
