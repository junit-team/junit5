plugins {
	`java-library-conventions`
}

description = "JUnit Platform Reporting"

dependencies {
	api(platform(project(":junit-bom")))
	api(libs.apiguardian)
	api(project(":junit-platform-launcher"))
}
