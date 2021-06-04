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
	mainJavaVersion = JavaVersion.VERSION_1_8
}

tasks {
	compileModule {
		options.release.set(8)
	}
}
