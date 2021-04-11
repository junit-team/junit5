plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite API"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.platform.commons)

	osgiVerification(projects.platform.commons)
}
