description = "JUnit Jupiter API"

dependencies {
	val ota4jVersion: String by project
	api("org.opentest4j:opentest4j:${ota4jVersion}")
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
