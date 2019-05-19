plugins {
	`java-library-conventions`
}

description = "JUnit Platform Engine API"

dependencies {
	api("org.apiguardian:apiguardian-api")
	api("org.opentest4j:opentest4j")

	api(project(":junit-platform-commons"))

	testImplementation("org.assertj:assertj-core")
}
