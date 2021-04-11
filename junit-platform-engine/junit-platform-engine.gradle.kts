plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Engine API"

dependencies {
	api(platform(project(":junit-bom")))
	api(libs.apiguardian)
	api(libs.opentest4j)
	api(project(":junit-platform-commons"))

	testImplementation(libs.assertj)
}
