import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
	`testing-conventions`
}

description = "JUnit Jupiter Migration Support"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api(libs.junit4)
	api(libs.apiguardian)
	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-jupiter-engine"))
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
				org.junit;version="[${libs.versions.junit4Min.get()},5)",\
				org.junit.platform.commons.logging;status=INTERNAL,\
				org.junit.rules;version="[${libs.versions.junit4Min.get()},5)",\
				*
		""")
	}
}
