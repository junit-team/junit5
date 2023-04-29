plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.junit4-compatibility")
}

description = "JUnit Platform Runner"

dependencies {
	api(platform(projects.junitBom))
	api(libs.junit4)
	api(projects.junitPlatformLauncher)
	api(projects.junitPlatformSuiteApi)

	compileOnlyApi(libs.apiguardian)

	implementation(projects.junitPlatformSuiteCommons)

	testImplementation(testFixtures(projects.junitPlatformEngine))
	testImplementation(testFixtures(projects.junitPlatformLauncher))

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks.jar {
	bundle {
		val importAPIGuardian: String by extra
		bnd("""
			# Import JUnit4 packages with a version
			Import-Package: \
				$importAPIGuardian,\
				org.junit.platform.commons.logging;status=INTERNAL,\
				org.junit.runner.*;version="[${libs.versions.junit4Min.get()},5)",\
				*
		""")
	}
}
