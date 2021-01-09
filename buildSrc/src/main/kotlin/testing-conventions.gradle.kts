import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.internal.os.OperatingSystem

plugins {
	id("org.gradle.test-retry")
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
		maxRetries.set(providers.gradleProperty("retries").map(String::toInt).orElse(2))
	}
	systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
	// Required until ASM officially supports the JDK 14
	systemProperty("net.bytebuddy.experimental", true)
	if (project.hasProperty("enableJFR")) {
		jvmArgs(
			"-XX:+UnlockDiagnosticVMOptions",
			"-XX:+DebugNonSafepoints",
			"-XX:StartFlightRecording=filename=${reports.junitXml.destination},dumponexit=true,settings=profile.jfc",
			"-XX:FlightRecorderOptions=stackdepth=1024"
		)
	}
	// Track OS as input so that tests are executed on all configured operating systems on CI
	inputs.property("os", OperatingSystem.current().familyName)
}

dependencies {
	"testImplementation"("org.assertj:assertj-core")
	"testImplementation"("org.mockito:mockito-junit-jupiter") {
		exclude(module = "junit-jupiter-engine")
	}

	if (project.name != "junit-jupiter-engine") {
		"testImplementation"(project(":junit-jupiter"))
	}
	"testImplementation"(testFixtures(project(":junit-jupiter-api")))

	"testRuntimeOnly"(project(":junit-platform-launcher"))
	"testRuntimeOnly"(project(":junit-platform-jfr"))

	"testRuntimeOnly"("org.apache.logging.log4j:log4j-core")
	"testRuntimeOnly"("org.apache.logging.log4j:log4j-jul")
}
