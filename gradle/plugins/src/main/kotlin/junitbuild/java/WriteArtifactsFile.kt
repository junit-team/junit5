package junitbuild.java

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class WriteArtifactsFile : DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val moduleVersions: SetProperty<ModuleVersionIdentifier>

    fun from(configuration: Provider<Configuration>) {
        moduleVersions.addAll(configuration.map {
            it.resolvedConfiguration.resolvedArtifacts.map { it.moduleVersion.id }
        })
    }

    @TaskAction
    fun writeFile() {
        outputFile.get().asFile.printWriter().use { out ->
            moduleVersions.get()
                .map { "${it.group}:${it.name}:${it.version}" }
                .sorted()
                .forEach(out::println)
        }
    }
}
