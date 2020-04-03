import java.util.spi.ToolProvider

plugins {
	`java-library-conventions`
	`shadow-conventions`
	`java-multi-release-sources`
	`java-repackage-jars`
}

description = "JUnit Platform Console"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-reporting"))

	shadowed(platform(project(":dependencies")))
	shadowed("info.picocli:picocli")
}

tasks {
	shadowJar {
		exclude("META-INF/versions/9/module-info.class")
		relocate("picocli", "org.junit.platform.console.shadow.picocli")
		from(projectDir) {
			include("LICENSE-picocli.md")
			into("META-INF")
		}
		from(sourceSets.mainRelease9.get().output.classesDirs)
		doLast {
			ToolProvider.findFirst("jar").get().run(System.out, System.err, "--update",
					"--file", archiveFile.get().asFile.absolutePath,
					"--main-class", "org.junit.platform.console.ConsoleLauncher")
		}
	}
	jar {
		manifest {
			attributes("Main-Class" to "org.junit.platform.console.ConsoleLauncher")
		}
	}

	// This jar contains some Java 9 code
	// (org.junit.platform.console.ConsoleLauncherToolProvider which implements
	// java.util.spi.ToolProvider which is @since 9).
	// So in order to resolve this, it can only run on Java 9
	osgiProperties {
		property("-runee", "JavaSE-9")
	}
}
