plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Launcher"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformEngine)

	compileOnlyApi(libs.apiguardian)
}
