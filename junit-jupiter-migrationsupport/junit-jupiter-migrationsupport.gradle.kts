plugins {
	`java-library-conventions`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Jupiter Migration Support"

dependencies {
	api("junit:junit")
	api("org.apiguardian:apiguardian-api")

	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-jupiter-engine"))
	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
}
