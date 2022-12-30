import org.junit.gradle.java.ExecJarAction

plugins {
	`java-library-conventions`
	`java-multi-release-sources`
	`java-repackage-jars`
	`java-test-fixtures`
}

description = "JUnit Platform Commons"

dependencies {
	api(platform(projects.junitBom))

	compileOnlyApi(libs.apiguardian)
}

tasks.jar {
	val release9ClassesDir = sourceSets.mainRelease9.get().output.classesDirs.singleFile
	inputs.dir(release9ClassesDir).withPathSensitivity(PathSensitivity.RELATIVE)
	doLast(objects.newInstance(ExecJarAction::class).apply {
		javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
		args.addAll(
			"--update",
			"--file", archiveFile.get().asFile.absolutePath,
			"--release", "9",
			"-C", release9ClassesDir.absolutePath, "."
		)
	})
}

tasks.codeCoverageClassesJar {
	exclude("org/junit/platform/commons/util/ModuleUtils.class")
}

eclipse {
	classpath {
		sourceSets -= project.sourceSets.mainRelease9.get()
	}
}
