plugins {
	java
}

// grab jupiter version from system environment
val jupiterVersion: String = System.getenv("JUNIT_JUPITER_VERSION")
val vintageVersion: String = System.getenv("JUNIT_VINTAGE_VERSION")
val platformVersion: String = System.getenv("JUNIT_PLATFORM_VERSION")

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-reporting:$platformVersion")
}

tasks.test {
	useJUnitPlatform()

	testLogging {
		events("passed", "skipped", "failed")
	}

	reports {
		html.required = true
	}

	val outputDir = reports.junitXml.outputLocation
	jvmArgumentProviders += CommandLineArgumentProvider {
		listOf(
			"-Djunit.platform.reporting.open.xml.enabled=true",
			"-Djunit.platform.reporting.output.dir=${outputDir.get().asFile.absolutePath}"
		)
	}

	doFirst {
		println("Using Java version: ${JavaVersion.current()}")
	}
}
