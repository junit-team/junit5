import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Vintage Engine"

dependencies {
	api(platform(project(":junit-bom")))

	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
	api(project(":junit-platform-engine"))
	api("junit:junit")

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
}

tasks.jar {
	withConvention(BundleTaskConvention::class) {
		bnd("""
			# Import JUnit4 packages with a version
			Import-Package: \
				!org.apiguardian.api,\
				junit.runner;version="[${Versions.junit4Min},5)",\
				org.junit;version="[${Versions.junit4Min},5)",\
				org.junit.experimental.categories;version="[${Versions.junit4Min},5)",\
				org.junit.internal.builders;version="[${Versions.junit4Min},5)",\
				org.junit.platform.commons.logging;status=INTERNAL,\
				org.junit.runner.*;version="[${Versions.junit4Min},5)",\
				org.junit.runners.model;version="[${Versions.junit4Min},5)",\
				*
		""")
	}
}
