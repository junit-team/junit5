plugins {
	`java-library-conventions`
}

description = "JUnit Platform Test Kit"

dependencies {
	api("org.apiguardian:apiguardian-api")
	api("org.assertj:assertj-core")
	api("org.opentest4j:opentest4j")

	api(project(":junit-platform-launcher"))
}
