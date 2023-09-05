plugins {
	id("junitbuild.java-library-conventions")
}

description = "JUnit Vintage Reporting Extensions"

dependencies {
	api(platform(projects.junitBom))
	api(libs.junit4)
	compileOnlyApi(libs.apiguardian)
	osgiVerification(projects.junitPlatformLauncher)
}
