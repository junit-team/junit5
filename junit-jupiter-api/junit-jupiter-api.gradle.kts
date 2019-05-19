plugins {
	`kotlin-library-conventions`
}

description = "JUnit Jupiter API"

dependencies {
	api("org.apiguardian:apiguardian-api")
	api("org.opentest4j:opentest4j")

	api(project(":junit-platform-commons"))

	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
}
