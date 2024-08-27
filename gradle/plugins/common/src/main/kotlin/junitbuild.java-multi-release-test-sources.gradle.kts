import junitbuild.extensions.capitalized

plugins {
	id("junitbuild.java-library-conventions")
}

listOf(21).forEach { javaVersion ->
	val sourceSet = sourceSets.register("testRelease${javaVersion}") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
		java {
			setSrcDirs(setOf("src/test/java${javaVersion}"))
		}
	}

	configurations {
		named(sourceSet.get().compileClasspathConfigurationName).configure {
			extendsFrom(configurations.testCompileClasspath.get())
		}
		named(sourceSet.get().runtimeClasspathConfigurationName).configure {
			extendsFrom(configurations.testRuntimeClasspath.get())
		}
	}

	tasks {
		val testTask = register<Test>("testRelease${javaVersion}") {
			group = "verification"
			description = "Runs the tests for Java ${javaVersion}"
			testClassesDirs = sourceSet.get().output.classesDirs
			classpath = sourceSet.get().runtimeClasspath
		}
		check {
			dependsOn(testTask)
		}
		named<JavaCompile>(sourceSet.get().compileJavaTaskName).configure {
			options.release = javaVersion
		}
		named<Checkstyle>("checkstyle${sourceSet.name.capitalized()}").configure {
			config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleTest.xml"))
		}
	}
}
