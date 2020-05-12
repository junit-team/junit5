plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Launcher"

dependencies {
	api(platform(project(":junit-bom")))

	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-engine"))

	implementation(project(":junit-platform-suite-api"))
}
