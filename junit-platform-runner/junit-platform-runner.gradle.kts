plugins {
	`java-library-conventions`
}

description = "JUnit Platform Runner"

dependencies {
	api("junit:junit:${Versions.junit4}")
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))
}
