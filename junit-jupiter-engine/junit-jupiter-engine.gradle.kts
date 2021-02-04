import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`kotlin-library-conventions`
	`testing-conventions`
	groovy
}

description = "JUnit Jupiter Engine"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-engine"))
	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	testImplementation(testFixtures(project(":junit-platform-commons")))
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	testImplementation("org.codehaus.groovy:groovy")
}

tasks {
	jar {
		withConvention(BundleTaskConvention::class) {
			bnd("""
				Provide-Capability:\
					org.junit.platform.engine;\
						org.junit.platform.engine='junit-jupiter';\
						version:Version="${'$'}{version_cleanup;${project.version}}"
			""")
		}
	}
}
