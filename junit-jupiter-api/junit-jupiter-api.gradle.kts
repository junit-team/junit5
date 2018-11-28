description = "JUnit Jupiter API"

dependencies {
	api("org.opentest4j:opentest4j:${Versions.ota4j}")
	api(project(":junit-platform-commons"))
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
	runtimeOnly(project(":junit-jupiter-engine"))
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.jupiter.api"
		)
	}
}
