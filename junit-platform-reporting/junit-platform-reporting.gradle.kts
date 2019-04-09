plugins {
	`java-library-conventions`
}

description = "JUnit Platform Reporting"

javaLibrary {
	automaticModuleName = "org.junit.platform.reporting"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
}
