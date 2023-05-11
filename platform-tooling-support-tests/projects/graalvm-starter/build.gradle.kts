plugins {
	java
	id("org.graalvm.buildtools.native")
}

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
			buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.LauncherConfig")
			buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.config.InstantiatingConfigurationParameterConverter")
			buildArgs.add("-H:+ReportExceptionStackTraces")
		}
	}
}
