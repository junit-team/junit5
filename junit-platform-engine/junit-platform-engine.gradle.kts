plugins {
	id("junitbuild.java-library-conventions")
	`java-test-fixtures`
}

description = "JUnit Platform Engine API"

dependencies {
	api(platform(projects.junitBom))
	api(libs.opentest4j)
	api(projects.junitPlatformCommons)
	api(libs.jfrpolyfill)

	compileOnlyApi(libs.apiguardian)

	testImplementation(libs.assertj)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
