import junitbuild.graalvm.NativeImagePropertiesExtension

plugins {
    `java-library`
}

val extension = extensions.create<NativeImagePropertiesExtension>("nativeImageProperties")
extension.initializeAtBuildTime.empty()

val outputDir = layout.buildDirectory.dir("generated/sources/nativeImage")

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
