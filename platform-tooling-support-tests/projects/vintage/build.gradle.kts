import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	java
}

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

dependencies {
	val junit4Version = System.getProperty("junit4Version", "4.12")
	testImplementation("junit:junit:$junit4Version")

	val vintageVersion = System.getenv("JUNIT_VINTAGE_VERSION") ?: "5.3.2"
	testImplementation("org.junit.vintage:junit-vintage-engine:$vintageVersion") {
		exclude(group = "junit")
		because("we want to override it to test against different versions")
	}
}

tasks.test {
	useJUnitPlatform()

	testLogging {
		events = setOf(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.FAILED)
		afterSuite(KotlinClosure2<TestDescriptor, TestResult, Any>({ _, result ->
			result.exception?.printStackTrace(System.out)
		}))
	}
}
