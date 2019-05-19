import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

tasks.named<Test>("test").configure {
	useJUnitPlatform {
		includeEngines("junit-jupiter")
	}
	include("**/*Test.class", "**/*Tests.class")
	testLogging {
		events = setOf(FAILED)
		exceptionFormat = FULL
	}
	systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
}

dependencies {
	"testImplementation"("org.assertj:assertj-core")
	"testImplementation"("org.mockito:mockito-junit-jupiter") {
		exclude(module = "junit-jupiter-engine")
	}

	if (project.name != "junit-jupiter-engine") {
		"testImplementation"(project(":junit-jupiter-api"))
		"testImplementation"(project(":junit-jupiter-params"))

		"testRuntimeOnly"(project(":junit-jupiter-engine"))
	}

	"testRuntimeOnly"(project(":junit-platform-launcher"))

	"testRuntimeOnly"("org.apache.logging.log4j:log4j-core")
	"testRuntimeOnly"("org.apache.logging.log4j:log4j-jul")
}
