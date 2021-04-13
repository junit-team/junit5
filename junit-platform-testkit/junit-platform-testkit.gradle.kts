plugins {
	`java-library-conventions`
}

description = "JUnit Platform Test Kit"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(libs.assertj)
	api(libs.opentest4j)
	api(projects.junitPlatformLauncher)
}
