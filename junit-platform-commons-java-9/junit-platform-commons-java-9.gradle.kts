import java.util.spi.ToolProvider

plugins {
	`java-library-conventions`
}

description = "JUnit Platform Commons - Java 9+ specific implementations"

apply(from = "$rootDir/gradle/testing.gradle.kts")

dependencies {
	implementation(project(":junit-platform-commons")) {
		because ("using types from the base version, e.g. JUnitException and Logger")
	}
}

javaLibrary {
	// Compiles against the public, supported and documented Java 9 API.
	mainJavaVersion = JavaVersion.VERSION_1_9
}

tasks.jar {
	enabled = true
	doLast {
		val commons = project(":junit-platform-commons")
		val archive = commons.tasks.jar.get().archiveFile.get().asFile.absolutePath
		println(commons)
		println(archive)
		ToolProvider.findFirst("jar").get().run(System.out, System.err,
				"--verbose",
				"--update",
				"--file", archive,
				"--release", "9",
				"-C", tasks.compileJava.get().destinationDir.absolutePath,
				".")
		ToolProvider.findFirst("jar").get().run(System.out, System.err,
				"--describe-module",
				"--file", archive)
	}
}
