plugins {
	java
}

// grab jupiter version from system environment
val jupiterVersion: String = System.getenv("JUNIT_JUPITER_VERSION")
val platformVersion: String = System.getenv("JUNIT_PLATFORM_VERSION")

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-reporting:$platformVersion")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(8)
	}
}

tasks.test {
	useJUnitPlatform()

	testLogging {
		events("passed", "skipped", "failed", "standardOut")
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
}
