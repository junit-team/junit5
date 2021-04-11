plugins {
	`kotlin-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Jupiter API"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(libs.opentest4j)
	api(projects.platform.commons)

	compileOnly(kotlin("stdlib"))
}
