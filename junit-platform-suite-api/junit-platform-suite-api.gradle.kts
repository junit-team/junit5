plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite API"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")

	osgiVerification(project(":junit-platform-commons"))
}
