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
	testImplementation(libs.jimfs)
	testImplementation(libs.junit4)
	testImplementation(libs.kotlinx.coroutines)
	testImplementation(libs.groovy4)
	testImplementation(libs.memoryfilesystem)
	testImplementation(testFixtures(projects.junitJupiterApi))

	osgiVerification(projects.junitPlatformLauncher)
}

tasks {
	test {
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
		systemProperty("developmentVersion", version)
	}
	jar {
		bundle {
			val platformVersion: String by rootProject.extra
			bnd("""
				Provide-Capability:\
					org.junit.platform.engine;\
						org.junit.platform.engine='junit-jupiter';\
						version:Version="${'$'}{version_cleanup;${project.version}}"
				Require-Capability:\
					org.junit.platform.launcher;\
						filter:='(&(org.junit.platform.launcher=junit-platform-launcher)(version>=${'$'}{version_cleanup;${platformVersion}})(!(version>=${'$'}{versionmask;+;${'$'}{version_cleanup;${platformVersion}}})))';\
						effective:=active
			""")
		}
	}
}
