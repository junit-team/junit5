description = "JUnit Platform Commons"

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.commons"
		)
	}
}
