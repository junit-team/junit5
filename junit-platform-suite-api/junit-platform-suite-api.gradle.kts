description = "JUnit Platform Suite API"

dependencies {
	api(project(":junit-platform-commons"))
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.suite.api"
		)
	}
}
