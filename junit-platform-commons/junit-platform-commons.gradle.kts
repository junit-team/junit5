import java.util.spi.ToolProvider

plugins {
	`java-library-conventions`
	`java-multi-release-sources`
	`java-repackage-jars`
}

description = "JUnit Platform Commons"

dependencies {
	internal(platform(project(":dependencies")))
	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
}

multiReleaseSources {
	releases.add(9)
}

tasks.jar {
	val release9ClassesDir = sourceSets["mainRelease9"].java.classesDirectory
	inputs.dir(release9ClassesDir).withPathSensitivity(PathSensitivity.RELATIVE)
	doLast {
		ToolProvider.findFirst("jar").get().run(System.out, System.err, "--update",
				"--file", archiveFile.get().asFile.absolutePath,
				"--release", "9",
				"-C", release9ClassesDir.get().asFile.absolutePath, ".")
	}
}

eclipse {
	classpath {
		sourceSets -= project.sourceSets["mainRelease9"]
	}
}
