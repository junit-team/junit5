plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite Commons"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(projects.junitPlatformLauncher)

	implementation(projects.junitPlatformEngine)
	implementation(projects.junitPlatformSuiteApi)
}
