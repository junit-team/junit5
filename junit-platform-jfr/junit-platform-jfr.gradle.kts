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
