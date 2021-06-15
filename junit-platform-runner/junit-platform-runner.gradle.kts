import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
}

description = "JUnit Platform Runner"

dependencies {
	api(platform(projects.junitBom))
	api(libs.junit4)
	api(libs.apiguardian)
	api(projects.junitPlatformLauncher)
	api(projects.junitPlatformSuiteApi)

	implementation(projects.junitPlatformSuiteCommons)

	testImplementation(testFixtures(projects.junitPlatformEngine))
	testImplementation(testFixtures(projects.junitPlatformLauncher))
}

tasks.jar {
	withConvention(BundleTaskConvention::class) {
		bnd("""
			# Import JUnit4 packages with a version
			Import-Package: \
				${extra["importAPIGuardian"]},\
				org.junit.platform.commons.logging;status=INTERNAL,\
				org.junit.runner.*;version="[${libs.versions.junit4Min.get()},5)",\
				*
		""")
	}
}
