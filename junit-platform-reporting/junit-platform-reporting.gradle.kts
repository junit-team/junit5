plugins {
	`java-library-conventions`
}

description = "JUnit Platform Reporting"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
}
