import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
}

description = "JUnit Platform Runner"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("junit:junit")
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))

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
				org.junit.runner.*;version="[${versions.junit4Min},5)",\
				*
		""")
	}
}
