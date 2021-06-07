plugins {
	`java-library-conventions`
}

description = "JUnit Platform Suite API"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(projects.junitPlatformCommons)

	osgiVerification(projects.junitPlatformCommons)
}
