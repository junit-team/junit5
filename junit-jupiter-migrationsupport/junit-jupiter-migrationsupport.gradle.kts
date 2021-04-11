import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
	`testing-conventions`
}

description = "JUnit Jupiter Migration Support"

dependencies {
	api(platform(projects.bom))
	api(libs.junit4)
	api(libs.apiguardian)
	api(projects.jupiter.api)

	testImplementation(projects.jupiter.engine)
	testImplementation(projects.platform.launcher)
	testImplementation(projects.platform.runner)
	testImplementation(projects.platform.testkit)
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
