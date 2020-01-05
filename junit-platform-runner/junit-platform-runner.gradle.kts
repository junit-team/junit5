import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Platform Runner"

dependencies {
	api(platform(project(":junit-bom")))

	api("junit:junit:[${Versions.junit4Min},)") {
		version {
			prefer(Versions.junit4)
		}
	}
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))

	testRuntimeOnly("org.apache.servicemix.bundles:org.apache.servicemix.bundles.junit:4.12_1")
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
				org.junit.runner.*;version="@${Versions.junit4Min}",\
				*
		""")
	}
}
