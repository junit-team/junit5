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
	releases.add(16)
}

val compileMainRelease16Java by tasks.existing(JavaCompile::class)

tasks.jar {
	val release16ClassesDir = sourceSets["mainRelease16"].java.classesDirectory
	inputs.dir(release16ClassesDir).withPathSensitivity(PathSensitivity.RELATIVE)
	doLast {
		exec {
			val javaHome = compileMainRelease16Java.get().javaCompiler.get().metadata.installationPath.asFile.absolutePath
			commandLine("$javaHome/bin/jar", "--update",
					"--file", archiveFile.get().asFile.absolutePath,
					"--release", "16",
					"-C", release16ClassesDir.get().asFile.absolutePath, ".")
		}
	}
}
