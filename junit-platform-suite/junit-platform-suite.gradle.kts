plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite (Aggregator)"

dependencies {
	api(projects.junitPlatformSuiteApi)
	implementation(projects.junitPlatformSuiteEngine)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
