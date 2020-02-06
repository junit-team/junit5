plugins {
	`java-library-conventions`
}

description = "JUnit Platform Test Kit"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api("org.assertj:assertj-core")
	api("org.opentest4j:opentest4j")
	api(project(":junit-platform-launcher"))
}
