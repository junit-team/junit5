plugins {
	`java-library-conventions`
}

description = "JUnit Platform Test Kit"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(libs.assertj)
	api(libs.opentest4j)
	api(projects.platform.launcher)
}
