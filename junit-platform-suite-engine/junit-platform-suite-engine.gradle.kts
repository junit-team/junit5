plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite Engine"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(projects.junitPlatformEngine)
	api(projects.junitPlatformSuiteApi)

	implementation(projects.junitPlatformSuiteCommons)
}
