plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Engine API"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(libs.opentest4j)
	api(projects.junitPlatformCommons)

	testImplementation(libs.assertj)
}
