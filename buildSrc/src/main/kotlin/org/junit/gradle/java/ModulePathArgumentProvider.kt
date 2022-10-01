package org.junit.gradle.java

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import org.gradle.process.CommandLineArgumentProvider

class ModulePathArgumentProvider(project: Project, modularProjects: List<Project>) : CommandLineArgumentProvider,
    Named {

    @get:CompileClasspath
    val modulePath: Provider<Configuration> = project.configurations.named("compileClasspath")

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val moduleSourceDirs: FileCollection = project.files(modularProjects.map { "${it.projectDir}/src/module" })

    override fun asArguments() = listOf(
        "--module-path",
        modulePath.get().asPath,
        "--module-source-path",
        moduleSourceDirs.asPath
    )

    @Internal
    override fun getName() = "module-path"
}
