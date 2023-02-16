plugins {
	id("junitbuild.java-library-conventions")
}

description = "JUnit Platform Suite (Aggregator)"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformSuiteApi)
	implementation(projects.junitPlatformSuiteEngine)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
