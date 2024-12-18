import junitbuild.graalvm.NativeImagePropertiesExtension

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
}

val outputDir = layout.buildDirectory.dir("resources/nativeImage")

val task = tasks.register<WriteProperties>("nativeImageProperties") {
    destinationFile = outputDir.map { it.file("META-INF/native-image/${project.group}/${project.name}/native-image.properties") }
    property("Args", extension.initializeAtBuildTime.map {
        if (it.isEmpty()) {
            ""
        } else {
            "--initialize-at-build-time=${it.joinToString(",")}"
        }
    })
}

sourceSets {
    main {
        output.dir(task.map { outputDir })
    }
}
