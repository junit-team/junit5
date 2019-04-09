plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite API"

javaLibrary {
	automaticModuleName = "org.junit.platform.suite.api"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
}
