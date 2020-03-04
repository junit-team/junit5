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
