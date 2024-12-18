plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.native-image-properties")
	`java-test-fixtures`
}

description = "JUnit Platform Engine API"

dependencies {
	api(platform(projects.junitBom))
	api(libs.opentest4j)
	api(projects.junitPlatformCommons)

	compileOnlyApi(libs.apiguardian)

	testImplementation(libs.assertj)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

nativeImageProperties {
	initializeAtBuildTime.addAll(
		"org.junit.platform.engine.TestDescriptor\$Type",
		"org.junit.platform.engine.UniqueId",
		"org.junit.platform.engine.UniqueId\$Segment",
		"org.junit.platform.engine.UniqueIdFormat",
		"org.junit.platform.engine.support.descriptor.ClassSource",
		"org.junit.platform.engine.support.descriptor.MethodSource",
		"org.junit.platform.engine.support.hierarchical.Node\$ExecutionMode",
	)
}
