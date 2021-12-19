plugins {
	`java-library-conventions`
	`shadow-conventions`
	`java-multi-release-sources`
	`java-repackage-jars`
}

description = "JUnit Platform Console"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformReporting)

	compileOnlyApi(libs.apiguardian)

	compileOnly(libs.openTestReporting)

	shadowed(libs.picocli)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks {
	shadowJar {
		val release17ClassesDir = sourceSets.mainRelease17.get().output.classesDirs.singleFile
		inputs.dir(release17ClassesDir).withPathSensitivity(PathSensitivity.RELATIVE)
		exclude("META-INF/versions/9/module-info.class")
		relocate("picocli", "org.junit.platform.console.shadow.picocli")
		from(projectDir) {
			include("LICENSE-picocli.md")
			into("META-INF")
		}
		from(sourceSets.mainRelease9.get().output.classesDirs)
		doLast {
			exec {
				executable = project.the<JavaToolchainService>().launcherFor(java.toolchain).get()
					.metadata.installationPath.file("bin/jar").asFile.absolutePath
				args(
					"--update",
					"--file", archiveFile.get().asFile.absolutePath,
					"--main-class", "org.junit.platform.console.ConsoleLauncher",
					"--release", "17",
					"-C", release17ClassesDir.absolutePath, "."
				)
			}
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

eclipse {
	classpath {
		sourceSets -= project.sourceSets.mainRelease9.get()
		sourceSets -= project.sourceSets.mainRelease17.get()
	}
}
