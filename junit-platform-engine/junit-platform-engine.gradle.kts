plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Engine API"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(libs.opentest4j)
	api(projects.platform.commons)

	testImplementation(libs.assertj)
}
