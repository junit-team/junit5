plugins {
	`java-library-conventions`
}

description = "JUnit Platform Flight Recorder Support"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformLauncher)

	compileOnlyApi(libs.apiguardian)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

javaLibrary {
	// --release 8 does not support jdk.jfr even though it was backported
	configureRelease = false
}

tasks {
	compileJava {
		javaCompiler.set(project.javaToolchains.compilerFor {
			languageVersion.set(JavaLanguageVersion.of(8))
		})
	}
	compileModule {
		options.release.set(11)
	}
}
