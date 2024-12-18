plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.native-image-properties")
}

description = "JUnit Platform Suite Engine"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformEngine)
	api(projects.junitPlatformSuiteApi)

	compileOnlyApi(libs.apiguardian)

	implementation(projects.junitPlatformSuiteCommons)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

nativeImageProperties {
	initializeAtBuildTime.addAll(
		"org.junit.platform.suite.engine.SuiteEngineDescriptor",
		"org.junit.platform.suite.engine.SuiteLauncher",
		"org.junit.platform.suite.engine.SuiteTestDescriptor",
		"org.junit.platform.suite.engine.SuiteTestEngine",
	)
}
