plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite API"

dependencies {
	api(platform(project(":junit-bom")))
	api(libs.apiguardian)
	api(project(":junit-platform-commons"))

	osgiVerification(project(":junit-platform-commons"))
}
