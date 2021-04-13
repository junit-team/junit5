plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Launcher"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(projects.junitPlatformEngine)
}
