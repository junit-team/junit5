import com.gradle.enterprise.gradleplugin.testretry.retry
import com.gradle.enterprise.gradleplugin.testselection.internal.PredictiveTestSelectionExtensionInternal
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.internal.os.OperatingSystem

plugins {
	`java-library`
	id("junitbuild.build-parameters")
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
	retry {
		maxRetries = buildParameters.testing.retries.orElse(if (buildParameters.ci) 2 else 0)
	}
	distribution {
		enabled.convention(buildParameters.junit.develocity.testDistribution.enabled && (!buildParameters.ci || System.getenv("GRADLE_ENTERPRISE_ACCESS_KEY").isNotBlank()))
		maxLocalExecutors = buildParameters.junit.develocity.testDistribution.maxLocalExecutors
		maxRemoteExecutors = buildParameters.junit.develocity.testDistribution.maxRemoteExecutors
		if (buildParameters.ci) {
			when {
				OperatingSystem.current().isLinux -> requirements.add("os=linux")
				OperatingSystem.current().isWindows -> requirements.add("os=windows")
				OperatingSystem.current().isMacOsX -> requirements.add("os=macos")
			}
		}
	}
	predictiveSelection {
		enabled = buildParameters.junit.develocity.predictiveTestSelection.enabled

		// Ensure PTS works when publishing Build Scans to scans.gradle.com
		this as PredictiveTestSelectionExtensionInternal
		server = uri("https://ge.junit.org")
	}
	systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
	// Required until ASM officially supports the JDK 14
	systemProperty("net.bytebuddy.experimental", true)
	if (buildParameters.testing.enableJFR) {
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
	if (buildParameters.ci) {
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
	testImplementation(dependencyFromLibs("testingAnnotations"))

	if (!project.name.startsWith("junit-jupiter")) {
		testImplementation(project(":junit-jupiter"))
	}

	testRuntimeOnly(project(":junit-platform-engine"))
	testRuntimeOnly(project(":junit-platform-jfr"))
	testRuntimeOnly(project(":junit-platform-reporting"))

	testRuntimeOnly(bundleFromLibs("log4j"))
	testRuntimeOnly(dependencyFromLibs("openTestReporting-events")) {
		because("it's required to run tests via IntelliJ which does not consumed the shadowed jar of junit-platform-reporting")
	}
}
