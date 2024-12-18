plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.native-image-properties")
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
	osgiVerification(libs.openTestReporting.tooling.spi)

	testFixturesApi(projects.junitJupiterApi)
}

nativeImageProperties {
	initializeAtBuildTime.addAll(
		"org.junit.platform.reporting.open.xml.OpenTestReportGeneratingListener",
		"org.junit.platform.reporting.shadow.org.opentest4j.reporting.events.api.DocumentWriter$1",
	)
}

tasks {
	shadowJar {
		listOf("events", "schema").forEach { name ->
			val packageName = "org.opentest4j.reporting.${name}"
			relocate(packageName, "org.junit.platform.reporting.shadow.${packageName}")
		}
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
