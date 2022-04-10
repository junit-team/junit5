plugins {
	`java-library-conventions`
}

description = "JUnit Platform Test Kit"

dependencies {
	api(libs.assertj)
	api(libs.opentest4j)
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
