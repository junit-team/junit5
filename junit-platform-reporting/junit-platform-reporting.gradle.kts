plugins {
	`java-library-conventions`
}

description = "JUnit Platform Reporting"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-launcher"))
}
