apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Jupiter Engine"

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.jupiter.engine"
		)
	}
}

val testArtifacts by configurations.creating {
	extendsFrom(configurations["testRuntime"])
}

val testJar by tasks.creating(Jar::class) {
	classifier = "test"
	from(sourceSets.getByName("test").output)
}

artifacts {
	add(testArtifacts.name, testJar)
}

dependencies {
	api(project(":junit-platform-engine"))
	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
}
