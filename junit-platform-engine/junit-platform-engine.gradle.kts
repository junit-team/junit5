plugins {
	`java-library-conventions`
}

description = "JUnit Platform Engine API"

javaLibrary {
	automaticModuleName = "org.junit.platform.engine"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
	api("org.opentest4j:opentest4j:${Versions.ota4j}")

	api(project(":junit-platform-commons"))

	testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
}
