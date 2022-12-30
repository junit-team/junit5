import com.gradle.enterprise.gradleplugin.testretry.retry
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.internal.os.OperatingSystem

plugins {
	`java-library`
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform {
		includeEngines("junit-jupiter")
	}
	include("**/*Test.class", "**/*Tests.class")
	testLogging {
		events = setOf(FAILED)
		exceptionFormat = FULL
	}
	val isCiServer = System.getenv("CI") != null
	retry {
		maxRetries.set(providers.gradleProperty("retries").map(String::toInt).orElse(if (isCiServer) 2 else 0))
	}
	distribution {
		enabled.convention(providers.gradleProperty("enableTestDistribution")
			.map(String::toBoolean)
			.map { enabled -> enabled && (!isCiServer || System.getenv("GRADLE_ENTERPRISE_ACCESS_KEY").isNotBlank()) }
			.orElse(false))
		maxLocalExecutors.set(providers.gradleProperty("testDistribution.maxLocalExecutors").map(String::toInt).orElse(1))
		maxRemoteExecutors.set(providers.gradleProperty("testDistribution.maxRemoteExecutors").map(String::toInt))
		if (isCiServer) {
			when {
				OperatingSystem.current().isLinux -> requirements.add("os=linux")
				OperatingSystem.current().isWindows -> requirements.add("os=windows")
				OperatingSystem.current().isMacOsX -> requirements.add("os=macos")
			}
		}
	}
	predictiveSelection {
		enabled.set(providers.gradleProperty("enablePredictiveTestSelection").map(String::toBoolean).orElse(true))
	}
	systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
	// Required until ASM officially supports the JDK 14
	systemProperty("net.bytebuddy.experimental", true)
	if (project.hasProperty("enableJFR")) {
		jvmArgs(
			"-XX:+UnlockDiagnosticVMOptions",
			"-XX:+DebugNonSafepoints",
			"-XX:StartFlightRecording=filename=${reports.junitXml.outputLocation.get()},dumponexit=true,settings=profile.jfc",
			"-XX:FlightRecorderOptions=stackdepth=1024"
		)
	}

	// Track OS as input so that tests are executed on all configured operating systems on CI
	trackOperationSystemAsInput()

	// Avoid passing unnecessary environment variables to the JVM (from GitHub Actions)
	if (isCiServer) {
		environment.remove("RUNNER_TEMP")
		environment.remove("GITHUB_ACTION")
	}

	jvmArgumentProviders += CommandLineArgumentProvider {
		listOf(
			"-Djunit.platform.reporting.open.xml.enabled=true",
			"-Djunit.platform.reporting.output.dir=${reports.junitXml.outputLocation.get().asFile.absolutePath}"
		)
	}
}

dependencies {
	testImplementation(dependencyFromLibs("assertj"))
	testImplementation(dependencyFromLibs("mockito"))

	if (!project.name.startsWith("junit-jupiter")) {
		testImplementation(project(":junit-jupiter"))
	}
	testImplementation(testFixtures(project(":junit-jupiter-api")))

	testRuntimeOnly(project(":junit-platform-engine"))
	testRuntimeOnly(project(":junit-platform-jfr"))
	testRuntimeOnly(project(":junit-platform-reporting"))

	testRuntimeOnly(bundleFromLibs("log4j"))
}
