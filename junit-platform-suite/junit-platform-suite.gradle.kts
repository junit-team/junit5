plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite (Aggregator)"

dependencies {
	api(platform(project(":junit-bom")))
	api(project(":junit-platform-suite-api"))
	implementation(project(":junit-platform-suite-engine"))
}
