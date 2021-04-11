plugins {
	`java-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Platform Launcher"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.platform.engine)
}
