package junitbuild.java

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.*
import org.gradle.process.CommandLineArgumentProvider
import javax.inject.Inject

abstract class ModulePathArgumentProvider @Inject constructor(project: Project, modularProjects: List<Project>) :
    CommandLineArgumentProvider, Named {

    @get:CompileClasspath
    abstract val modulePath: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val moduleSourceDirs: ConfigurableFileCollection

    init {
        modulePath.from(project.configurations.named("compileClasspath"))
        modularProjects.forEach {
            moduleSourceDirs.from(project.files("${it.projectDir}/src/module"))
        }
    }

    override fun asArguments() = listOf(
        "--module-path",
        modulePath.asPath,
        "--module-source-path",
        moduleSourceDirs.asPath
    )

    @Internal
    override fun getName() = "module-path"
}
