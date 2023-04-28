plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.shadow-conventions")
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
	compileModule {
		options.compilerArgs.addAll(listOf(
			"--add-modules", "org.opentest4j.reporting.events",
			"--add-reads", "${javaModuleName}=org.opentest4j.reporting.events"
		))
	}
}
