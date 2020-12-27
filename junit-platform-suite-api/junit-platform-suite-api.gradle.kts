plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite API"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-commons"))

	osgiVerification(project(":junit-platform-commons"))
}
