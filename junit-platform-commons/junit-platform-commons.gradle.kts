plugins {
	`java-library-conventions`
}

description = "JUnit Platform Commons"

javaLibrary {
	automaticModuleName = "org.junit.platform.commons"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
}
