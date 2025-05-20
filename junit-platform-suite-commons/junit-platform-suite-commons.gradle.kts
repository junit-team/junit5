plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.java-nullability-conventions")
}

description = "JUnit Platform Suite Commons"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)
	compileOnly(libs.jspecify)

	implementation(projects.junitPlatformEngine)
	implementation(projects.junitPlatformSuiteApi)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks.compileJava {
	options.compilerArgs.add("-Xlint:-module") // due to qualified exports
}
