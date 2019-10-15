plugins {
	`java-library-conventions`
}

description = "JUnit Platform Runner"

dependencies {
	api(platform(project(":junit-bom")))

	api("junit:junit:${Versions.junit4}")
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))
}
