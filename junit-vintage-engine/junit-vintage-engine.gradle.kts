plugins {
	`java-library-conventions`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Vintage Engine"

val junit_4_13 by configurations.creating {
	extendsFrom(configurations.testRuntimeClasspath.get())
}

dependencies {
	api(platform(project(":junit-bom")))

	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
	api(project(":junit-platform-engine"))
	api("junit:junit:${Versions.junit4}")

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	junit_4_13("junit:junit:4.13-rc-1")
}

tasks {
	val test_4_13 by registering(Test::class) {
		classpath -= configurations.testRuntimeClasspath.get()
		classpath += junit_4_13
	}
	check {
		dependsOn(test_4_13)
	}
}
