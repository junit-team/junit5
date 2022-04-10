plugins {
	`java-library-conventions`
}

description = "JUnit Jupiter (Aggregator)"

dependencies {
	api(projects.junitJupiterApi)
	api(projects.junitJupiterParams)

	runtimeOnly(projects.junitJupiterEngine)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
