plugins {
	`java-library-conventions`
}

description = "JUnit Platform Launcher"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-engine"))
}
