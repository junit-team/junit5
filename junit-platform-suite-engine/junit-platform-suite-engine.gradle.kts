plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite Engine"

dependencies {
	api(projects.junitPlatformEngine)
	api(projects.junitPlatformSuiteApi)

	compileOnlyApi(libs.apiguardian)

	implementation(projects.junitPlatformSuiteCommons)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
