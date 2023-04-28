plugins {
	id("junitbuild.java-library-conventions")
}

description = "JUnit Platform Suite Commons"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)

	implementation(projects.junitPlatformEngine)
	implementation(projects.junitPlatformSuiteApi)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
