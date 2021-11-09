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
	configureRelease = false
}

tasks {
	compileJava {
		javaCompiler.set(project.the<JavaToolchainService>().compilerFor {
			languageVersion.set(JavaLanguageVersion.of(8))
		})
	}
	compileModule {
		options.release.set(11)
	}
}
