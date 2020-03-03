plugins {
	id("junitbuild.java-library-conventions")
	`java-test-fixtures`
	`java-multi-release-sources`
}

description = "JUnit Platform Engine API"

dependencies {
	api(platform(projects.junitBom))
	api(libs.opentest4j)
	api(projects.junitPlatformCommons)

	compileOnlyApi(libs.apiguardian)

	testImplementation(libs.assertj)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
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
