plugins {
	java
	id("org.graalvm.buildtools.native")
}

val jupiterVersion: String by project
val platformVersion: String by project

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
