package junitbuild.java

import javaModuleName
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.process.CommandLineArgumentProvider
import javax.inject.Inject

abstract class PatchModuleArgumentProvider @Inject constructor(compiledProject: Project, patchModuleProject: Project) :
    CommandLineArgumentProvider, Named {

    @get:Input
    abstract val module: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val patch: ConfigurableFileCollection

    init {
        module.convention(patchModuleProject.javaModuleName)
        patch.from(compiledProject.provider {
            if (patchModuleProject == compiledProject)
                compiledProject.files(compiledProject.the<SourceSetContainer>().matching { it.name.startsWith("main") }
                    .map { it.output })
            else
                patchModuleProject.files(patchModuleProject.the<SourceSetContainer>()["main"].java.srcDirs)
        })
    }

    override fun asArguments(): List<String> {
        val path = patch.filter { it.exists() }.asPath
        if (path.isEmpty()) {
            return emptyList()
        }
        return listOf("--patch-module", "${module.get()}=$path")
    }

    @Internal
    override fun getName() = "patch-module(${module.get()})"
}
