plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Launcher"

dependencies {
	api(platform(project(":junit-bom")))
	api(libs.apiguardian)
	api(project(":junit-platform-engine"))
}
