plugins {
	`java-library-conventions`
	`shadow-conventions`
}

description = "JUnit Platform Reporting"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)

	shadowed(libs.openTestReporting.events)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks {
	shadowJar {
		relocate("org.opentest4j.reporting", "org.junit.platform.reporting.shadow.org.opentest4j.reporting")
		from(projectDir) {
			include("LICENSE-open-test-reporting.md")
			into("META-INF")
		}
	}
}
