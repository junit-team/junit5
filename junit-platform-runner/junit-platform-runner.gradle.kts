import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
}

description = "JUnit Platform Runner"

dependencies {
	api(platform(project(":junit-bom")))
	api(libs.junit4)
	api(libs.apiguardian)
	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))

	implementation(project(":junit-platform-suite-commons"))

	testImplementation(testFixtures(project(":junit-platform-engine")))
	testImplementation(testFixtures(project(":junit-platform-launcher")))
}

tasks.jar {
	withConvention(BundleTaskConvention::class) {
		bnd("""
			# Import JUnit4 packages with a version
			Import-Package: \
				!org.apiguardian.api,\
				org.junit.platform.commons.logging;status=INTERNAL,\
				org.junit.runner.*;version="[${libs.versions.junit4Min.get()},5)",\
				*
		""")
	}
}
