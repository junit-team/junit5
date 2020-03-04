import junitbuild.java.ExecJarAction

plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.java-multi-release-sources")
	id("junitbuild.java-repackage-jars")
	`java-test-fixtures`
}

description = "JUnit Platform Commons"

dependencies {
	api(platform(projects.junitBom))

	compileOnlyApi(libs.apiguardian)
}

multiReleaseSources {
	releases.add(9)
}

tasks.jar {
	val release9ClassesDir = sourceSets["mainRelease9"].java.classesDirectory
	inputs.dir(release9ClassesDir).withPathSensitivity(PathSensitivity.RELATIVE)
	doLast(objects.newInstance(ExecJarAction::class).apply {
		javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
		args.addAll(
			"--update",
			"--file", archiveFile.get().asFile.absolutePath,
			"--release", "9",
			"-C", release9ClassesDir.get().asFile.absolutePath, "."
		)
	})
}

tasks.codeCoverageClassesJar {
	exclude("org/junit/platform/commons/util/ModuleUtils.class")
}

eclipse {
	classpath {
		sourceSets -= project.sourceSets["mainRelease9"]
	}
}
