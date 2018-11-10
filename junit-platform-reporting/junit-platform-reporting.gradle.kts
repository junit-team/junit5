description = "JUnit Platform Reporting"

dependencies {
	api(project(":junit-platform-launcher"))
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
				"Automatic-Module-Name" to "org.junit.platform.reporting"
		)
	}
}
