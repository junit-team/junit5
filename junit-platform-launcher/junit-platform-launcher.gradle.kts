plugins {
	`java-library-conventions`
}

description = "JUnit Platform Launcher"

javaLibrary {
	automaticModuleName = "org.junit.platform.launcher"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-engine"))
}
