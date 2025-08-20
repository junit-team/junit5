import junitbuild.java.UpdateJarAction

plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.java-nullability-conventions")
}

description = "JUnit Aggregator"

dependencies {
	api(projects.junitJupiter)
	compileOnlyApi(projects.junitJupiterEngine)
	api(projects.junitPlatformLauncher)
	implementation(projects.junitPlatformConsole)
}

tasks {
	jar {
		manifest {
			attributes("Main-Class" to "org.junit.aggregator.JUnit")
		}
		doLast(objects.newInstance(UpdateJarAction::class).apply {
			javaLauncher = project.javaToolchains.launcherFor(java.toolchain)
			args.addAll(
				"--file", archiveFile.get().asFile.absolutePath,
				"--main-class", "org.junit.aggregator.JUnit",
			)
		})
	}
}
