plugins {
	`java-library-conventions`
}

description = "JUnit Jupiter (Aggregator)"

dependencies {
	api(platform(projects.bom))
	api(projects.jupiter.api)
	api(projects.jupiter.params)

	runtimeOnly(projects.jupiter.engine)
}
