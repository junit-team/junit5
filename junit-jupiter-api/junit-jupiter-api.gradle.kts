plugins {
	`kotlin-library-conventions`
	`java-test-fixtures`
}

description = "JUnit Jupiter API"

dependencies {
	api(platform(project(":junit-bom")))
	api(libs.apiguardian)
	api(libs.opentest4j)
	api(project(":junit-platform-commons"))

	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
}
