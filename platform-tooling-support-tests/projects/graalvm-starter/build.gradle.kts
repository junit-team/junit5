plugins {
	java
	id("org.graalvm.buildtools.native")
}

val jupiterVersion: String by project
val platformVersion: String by project
val vintageVersion: String by project

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
	testImplementation("junit:junit:4.13.2")
	testImplementation("org.junit.platform:junit-platform-suite:$platformVersion")
	testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$vintageVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-reporting:$platformVersion")
}

tasks.test {
	useJUnitPlatform {
		includeEngines("junit-platform-suite")
	}

	val outputDir = reports.junitXml.outputLocation
	jvmArgumentProviders += CommandLineArgumentProvider {
		listOf(
			"-Djunit.platform.reporting.open.xml.enabled=true",
			"-Djunit.platform.reporting.output.dir=${outputDir.get().asFile.absolutePath}"
		)
	}
}

graalvmNative {
	binaries {
		named("test") {
			buildArgs.add("--strict-image-heap")
			buildArgs.add("-H:+ReportExceptionStackTraces")
		}
	}
}
