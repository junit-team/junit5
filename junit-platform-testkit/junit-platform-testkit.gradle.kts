plugins {
	`java-library-conventions`
}

description = "JUnit Platform Test Kit"

dependencies {
	api(platform(project(":junit-bom")))
	api(libs.apiguardian)
	api(libs.assertj)
	api(libs.opentest4j)
	api(project(":junit-platform-launcher"))
}
