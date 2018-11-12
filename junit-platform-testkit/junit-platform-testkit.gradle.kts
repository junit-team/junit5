description = "JUnit Platform Test Kit"

dependencies {
	api(project(":junit-platform-launcher"))
	api("org.opentest4j:opentest4j:${Versions.ota4j}")
	api("org.assertj:assertj-core:${Versions.assertJ}")
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.testkit"
		)
	}
}
