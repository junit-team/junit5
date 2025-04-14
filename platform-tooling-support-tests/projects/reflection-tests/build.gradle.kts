plugins {
	java
}

val junitVersion: String by project

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-reporting:$junitVersion")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

tasks.test {
	useJUnitPlatform()

	testLogging {
		events("failed", "standardOut")
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
