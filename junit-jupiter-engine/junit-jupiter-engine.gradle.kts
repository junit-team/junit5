plugins {
	`kotlin-library-conventions`
	groovy
	`java-multi-release-sources`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Jupiter Engine"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-engine"))
	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
	testImplementation("org.codehaus.groovy:groovy-all")
}

tasks.jar {
	doLast {
		exec {
			val javaHome: String by project
			commandLine("$javaHome/bin/jar", "--update",
					"--file", archiveFile.get().asFile.absolutePath,
					"--release", "15",
					// TODO getting the firstfile in classesDirs is a hack
					"-C", sourceSets.mainRelease15.get().output.classesDirs.files.first().absolutePath, ".")
		}
	}
}
