plugins {
	`java-library-conventions`
}

description = "JUnit Platform Reporting"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)

	implementation("org.opentest4j.reporting:lib")

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
