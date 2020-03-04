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

multiReleaseSources {
	releases.add(15)
}

tasks.jar {
	val release15ClassesDir = sourceSets["mainRelease15"].java.classesDirectory
	inputs.dir(release15ClassesDir).withPathSensitivity(PathSensitivity.RELATIVE)
	doLast {
		exec {
			val javaHome: String by project
			commandLine("$javaHome/bin/jar", "--update",
					"--file", archiveFile.get().asFile.absolutePath,
					"--release", "15",
					"-C", release15ClassesDir.get().asFile.absolutePath, ".")
		}
	}
}
