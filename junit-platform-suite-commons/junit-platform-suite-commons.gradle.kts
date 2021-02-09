plugins {
	`java-library-conventions`
	`testing-conventions`
}

description = "JUnit Platform Suite Commons"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-launcher"))

	implementation(project(":junit-platform-engine"))
	implementation(project(":junit-platform-suite-api"))

	testImplementation(project(":junit-jupiter-engine"))
}
