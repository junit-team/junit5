plugins {
	`java-library-conventions`
}

description = "JUnit Jupiter (Aggregator)"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api(project(":junit-jupiter-api"))
	api(project(":junit-jupiter-params"))

	runtimeOnly(project(":junit-jupiter-engine"))
}
