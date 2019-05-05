plugins {
	`java-library-conventions`
}

description = "JUnit Jupiter (Aggregator)"

dependencies {
	api(project(":junit-jupiter-api"))
	api(project(":junit-jupiter-params"))
	runtimeOnly(project(":junit-jupiter-engine"))
}
