plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite API"

dependencies {
	api(projects.junitPlatformCommons)

	compileOnlyApi(libs.apiguardian)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
