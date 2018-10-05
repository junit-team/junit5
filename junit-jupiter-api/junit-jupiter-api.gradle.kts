description = "JUnit Jupiter API"

dependencies {
	api("org.opentest4j:opentest4j:${Versions.ota4j}")
	api(project(":junit-platform-commons"))
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.jupiter.api"
		)
	}
}
