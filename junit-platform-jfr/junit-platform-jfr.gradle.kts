plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.java-nullability-conventions")
}

description = "JUnit Platform Flight Recorder Support"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)
	compileOnly(libs.jspecify)

	if (java.toolchain.implementation.orNull == JvmImplementation.J9) {
		compileOnly(libs.jfrPolyfill) {
			because("OpenJ9 does not include JFR")
		}
	}

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}
