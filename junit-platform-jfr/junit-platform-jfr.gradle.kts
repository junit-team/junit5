plugins {
	`java-library-conventions`
}

description = "JUnit Platform Flight Recorder Support"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(projects.junitPlatformLauncher)
}

javaLibrary {
	mainJavaVersion = JavaVersion.VERSION_11
}

tasks {
	compileModule {
		options.release.set(9)
	}
}
