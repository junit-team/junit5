import net.ltgt.gradle.errorprone.errorprone

plugins {
	id("junitbuild.java-library-conventions")
	id("net.ltgt.errorprone")
}

dependencies {
	compileOnly(dependencyFromLibs("errorprone-annotations"))
	errorprone(dependencyFromLibs("errorprone-core"))
}

tasks {
	compileTestJava {
		options.errorprone.isEnabled.set(false)
	}
	named<JavaCompile>("compileModule") {
		options.errorprone.isEnabled.set(false)
		options.compilerArgumentProviders += CommandLineArgumentProvider {
			listOf(
				"--add-modules", "com.google.errorprone.annotations",
				"--add-reads", "org.junit.jupiter.engine=com.google.errorprone.annotations"
			)
		}
	}
}
