plugins {
	`java-library-conventions`
}

description = "JUnit Platform Test Kit"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
	api("org.assertj:assertj-core:${Versions.assertJ}")
	api("org.opentest4j:opentest4j:${Versions.ota4j}")

	api(project(":junit-platform-launcher"))
}
