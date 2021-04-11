plugins {
	`java-library-conventions`
}

description = "JUnit Platform Flight Recorder Support"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.platform.launcher)
}

javaLibrary {
	mainJavaVersion = JavaVersion.VERSION_11
}

tasks {
	compileModule {
		options.release.set(11)
	}
}
