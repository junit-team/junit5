import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
}

description = "JUnit Platform Runner"

dependencies {
	api(platform(projects.bom))
	api(libs.junit4)
	api(libs.apiguardian)
	api(projects.platform.launcher)
	api(projects.platform.suite.api)

	implementation(projects.platform.suite.commons)

	testImplementation(testFixtures(projects.platform.engine))
	testImplementation(testFixtures(projects.platform.launcher))
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
