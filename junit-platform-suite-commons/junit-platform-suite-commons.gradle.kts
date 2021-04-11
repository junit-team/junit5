plugins {
	`java-library-conventions`
	`testing-conventions`
}

description = "JUnit Platform Suite Commons"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.platform.launcher)

	implementation(projects.platform.engine)
	implementation(projects.platform.suite.api)

	testImplementation(projects.jupiter.engine)
}
