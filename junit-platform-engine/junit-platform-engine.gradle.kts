plugins {
	`java-library-conventions`
	`java-test-fixtures`
	`java-multi-release-sources`
}

description = "JUnit Platform Engine API"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api("org.opentest4j:opentest4j")
	api(project(":junit-platform-commons"))

	testImplementation("org.assertj:assertj-core")
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
