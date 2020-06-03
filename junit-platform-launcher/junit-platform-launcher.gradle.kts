plugins {
	`java-library-conventions`
	`java-multi-release-sources`
	`java-repackage-jars`
	`java-test-fixtures`
}

description = "JUnit Platform Launcher"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-engine"))
}
