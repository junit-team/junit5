import java.util.spi.ToolProvider

plugins {
	`java-library-conventions`
}

description = "JUnit Platform Commons"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
}

tasks.jar {
	doLast {
		ToolProvider.findFirst("jar").get().run(System.out, System.err, "--update",
				"--file", archiveFile.get().asFile.absolutePath,
				"--release", "9",
				"-C", "$buildDir/classes/java/module/org.junit.platform.commons", "org")
	}
}
