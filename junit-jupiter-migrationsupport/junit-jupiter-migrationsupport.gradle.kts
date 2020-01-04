import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Jupiter Migration Support"

dependencies {
	api(platform(project(":junit-bom")))

	api("junit:junit:[${Versions.junit4Min},)") {
		version {
			prefer(Versions.junit4)
		}
	}
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-jupiter-engine"))
	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))

	testRuntimeOnly("org.apache.servicemix.bundles:org.apache.servicemix.bundles.junit:4.12_1")
}

tasks.jar {
	withConvention(BundleTaskConvention::class) {
		bnd("""
			# Import JUnit4 packages with a version
			Import-Package: \
				!org.apiguardian.api,\
				org.junit;version="@${Versions.junit4Min}",\
				org.junit.platform.commons.logging;status=INTERNAL,\
				org.junit.rules;version="@${Versions.junit4Min}",\
				*
		""")
	}
}
