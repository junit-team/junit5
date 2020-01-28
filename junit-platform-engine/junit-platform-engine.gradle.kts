plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Engine API"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api("org.opentest4j:opentest4j")
	api(project(":junit-platform-commons"))

	testImplementation("org.assertj:assertj-core")
}
