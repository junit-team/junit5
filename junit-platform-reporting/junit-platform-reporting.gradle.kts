plugins {
	`java-library-conventions`
}

description = "JUnit Platform Reporting"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.platform.launcher)
}
