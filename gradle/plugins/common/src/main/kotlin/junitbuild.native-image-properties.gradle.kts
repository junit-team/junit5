import junitbuild.graalvm.NativeImagePropertiesExtension
import java.util.zip.ZipFile

plugins {
	`java-library`
}

val extension = extensions.create<NativeImagePropertiesExtension>("nativeImageProperties").apply {
	val resourceFile: RegularFile = layout.projectDirectory.file("src/nativeImage/initialize-at-build-time")
	if (resourceFile.asFile.exists()) {
		initializeAtBuildTime.convention(providers.fileContents(resourceFile).asText.map { it.trim().lines() })
	} else {
		initializeAtBuildTime.empty()
	}
	initializeAtBuildTime.finalizeValueOnRead()
}

val outputDir = layout.buildDirectory.dir("resources/nativeImage")

val propertyFileTask = tasks.register<WriteProperties>("nativeImageProperties") {
	destinationFile = outputDir.map { it.file("META-INF/native-image/${project.group}/${project.name}/native-image.properties") }
	property("Args", extension.initializeAtBuildTime.map {
		if (it.isEmpty()) {
			""
		} else {
			"--initialize-at-build-time=${it.joinToString(",")}"
		}
	})
}

val validationTask = tasks.register("validateNativeImageProperties") {
	dependsOn(tasks.jar)
	doLast {
		val missingClasses = ZipFile(tasks.jar.get().archiveFile.get().asFile).use { zipFile ->
			extension.initializeAtBuildTime.get().filter { className ->
				zipFile.entries().asSequence().none {
					it.name == className.replace('.', '/') + ".class"
				}
			}
		}
		if (missingClasses.isNotEmpty()) {
			throw GradleException("The following classes were specified as initialized-at-build-time but do not exist (you should probably remove them from nativeImageProperties.initializeAtBuildTime): $missingClasses")
		}
	}
}

tasks.check {
	dependsOn(validationTask)
}

sourceSets {
	main {
		output.dir(propertyFileTask.map { outputDir })
	}
}
