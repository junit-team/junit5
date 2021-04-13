plugins {
	`kotlin-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Jupiter API"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(libs.opentest4j)
	api(projects.junitPlatformCommons)

	compileOnly(kotlin("stdlib"))
}
