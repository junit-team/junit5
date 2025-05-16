import junitbuild.extensions.javaModuleName
import junitbuild.java.UpdateJarAction

plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.shadow-conventions")
}

description = "JUnit Platform Console"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformReporting)

	compileOnlyApi(libs.apiguardian)

	shadowed(libs.picocli)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
	osgiVerification(libs.openTestReporting.tooling.spi)
}

tasks {
	compileJava {
		options.compilerArgs.addAll(listOf(
			"--add-modules", "info.picocli",
			"--add-reads", "${javaModuleName}=info.picocli"
		))
	}
	javadoc {
		(options as StandardJavadocDocletOptions).apply {
			addStringOption("-add-modules", "info.picocli")
			addStringOption("-add-reads", "${javaModuleName}=info.picocli")
		}
	}
	shadowJar {
		exclude("META-INF/versions/9/module-info.class")
		relocate("picocli", "org.junit.platform.console.shadow.picocli")
		from(projectDir) {
			include("LICENSE-picocli.md")
			into("META-INF")
		}
		doLast(objects.newInstance(UpdateJarAction::class).apply {
			javaLauncher = project.javaToolchains.launcherFor(java.toolchain)
			args.addAll(
				"--file", archiveFile.get().asFile.absolutePath,
				"--main-class", "org.junit.platform.console.ConsoleLauncher",
			)
		})
	}
	codeCoverageClassesJar {
		exclude("org/junit/platform/console/options/ConsoleUtils.class")
	}
	jar {
		manifest {
			attributes("Main-Class" to "org.junit.platform.console.ConsoleLauncher")
		}
	}
}
