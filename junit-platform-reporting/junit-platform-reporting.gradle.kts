plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.shadow-conventions")
	`java-test-fixtures`
}

description = "JUnit Platform Reporting"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)
	compileOnlyApi(libs.openTestReporting.tooling.spi)

	shadowed(libs.openTestReporting.events)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)

	testFixturesApi(projects.junitJupiterApi)
}

tasks {
	shadowJar {
		relocate("org.opentest4j.reporting.events", "org.junit.platform.reporting.shadow.org.opentest4j.reporting.events")
		from(projectDir) {
			include("LICENSE-open-test-reporting.md")
			into("META-INF")
		}
	}
	compileModule {
		options.compilerArgs.addAll(listOf(
			"--add-modules", "org.opentest4j.reporting.events",
			"--add-reads", "${javaModuleName}=org.opentest4j.reporting.events"
		))
	}
}
