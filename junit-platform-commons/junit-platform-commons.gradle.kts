import java.util.spi.ToolProvider

plugins {
	`java-library-conventions`
}

description = "JUnit Platform Commons"

dependencies {
	api(platform(project(":junit-bom")))

	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
}

tasks.jar {
	doLast {
		ToolProvider.findFirst("jar").get().run(System.out, System.err, "--update",
				"--file", archiveFile.get().asFile.absolutePath,
				"--release", "9",
				"-C", sourceSets.mainRelease9.get().output.classesDirs.singleFile.absolutePath, ".")
	}
}

eclipse {
	classpath {
		sourceSets -= project.sourceSets.mainRelease9.get()
	}
}
