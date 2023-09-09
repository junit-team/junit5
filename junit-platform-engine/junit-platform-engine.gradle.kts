plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.java-multi-release-sources")
	`java-test-fixtures`
}

description = "JUnit Platform Engine API"

dependencies {
	api(platform(projects.junitBom))
	api(libs.opentest4j)
	api(projects.junitPlatformCommons)

	compileOnlyApi(libs.apiguardian)

	testImplementation(libs.assertj)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks.jar {
	val release21ClassesDir = project.sourceSets.mainRelease21.get().output.classesDirs.singleFile
	inputs.dir(release21ClassesDir).withPathSensitivity(PathSensitivity.RELATIVE)
	doLast(objects.newInstance(junitbuild.java.UpdateJarAction::class).apply {
		javaLauncher.set(project.javaToolchains.launcherFor {
			languageVersion.set(java.toolchain.languageVersion.map {
				if (it.canCompileOrRun(21)) it else JavaLanguageVersion.of(21)
			})
		})
		args.addAll(
			"--release", "21",
			"-C", release21ClassesDir.absolutePath, "."
		)
	})
}

eclipse {
	classpath {
		sourceSets -= project.sourceSets.mainRelease21.get()
	}
}
