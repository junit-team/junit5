import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	id("junitbuild.kotlin-library-conventions")
	id("junitbuild.testing-conventions")
	groovy
	`java-test-fixtures`
}

description = "JUnit Jupiter Engine"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformEngine)
	api(projects.junitJupiterApi)

	compileOnlyApi(libs.apiguardian)

	testImplementation(projects.junitPlatformLauncher)
	testImplementation(projects.junitPlatformSuiteEngine)
	testImplementation(projects.junitPlatformTestkit)
	testImplementation(testFixtures(projects.junitPlatformCommons))
	testImplementation(kotlin("stdlib"))
	testImplementation(libs.junit4)
	testImplementation(libs.kotlinx.coroutines)
	testImplementation(libs.groovy4)

	osgiVerification(projects.junitPlatformLauncher)
}

tasks {
	test {
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
		systemProperty("developmentVersion", version)
	}
}

tasks {
	jar {
		bundle {
			bnd("""
				Provide-Capability:\
					org.junit.platform.engine;\
						org.junit.platform.engine='junit-jupiter';\
						version:Version="${'$'}{version_cleanup;${project.version}}"
				Require-Capability:\
					org.junit.platform.launcher;\
						filter:='(&(org.junit.platform.launcher=junit-platform-launcher)(version>=${'$'}{version_cleanup;${rootProject.property("platformVersion")!!}})(!(version>=${'$'}{versionmask;+;${'$'}{version_cleanup;${rootProject.property("platformVersion")!!}}})))';\
						effective:=active
			""")
		}
	}
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
