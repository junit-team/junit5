plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.junit4-compatibility")
	id("junitbuild.testing-conventions")
}

description = "JUnit Jupiter Migration Support"

dependencies {
	api(platform(projects.junitBom))
	api(libs.junit4)
	api(projects.junitJupiterApi)

	compileOnlyApi(libs.apiguardian)

	testImplementation(projects.junitJupiterEngine)
	testImplementation(projects.junitPlatformLauncher)
	testImplementation(projects.junitPlatformSuiteEngine)
	testImplementation(projects.junitPlatformTestkit)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks.jar {
	bundle {
		val importAPIGuardian: String by extra
		bnd("""
			# Import JUnit4 packages with a version
			Import-Package: \
				$importAPIGuardian,\
				org.junit;version="[${libs.versions.junit4Min.get()},5)",\
				org.junit.platform.commons.logging;status=INTERNAL,\
				org.junit.rules;version="[${libs.versions.junit4Min.get()},5)",\
				*
		""")
	}
}
