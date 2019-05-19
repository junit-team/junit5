plugins {
	`java-library-conventions`
}

description = "JUnit Platform Runner"

dependencies {
	api("junit:junit")
	api("org.apiguardian:apiguardian-api")

	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))
}
