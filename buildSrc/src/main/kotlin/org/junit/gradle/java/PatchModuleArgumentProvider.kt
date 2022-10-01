package org.junit.gradle.java

import javaModuleName
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.process.CommandLineArgumentProvider

class PatchModuleArgumentProvider(compiledProject: Project, patchModuleProject: Project) : CommandLineArgumentProvider,
	Named {

	@get:Input
	val module: String = patchModuleProject.javaModuleName

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	val patch: Provider<FileCollection> = compiledProject.provider {
		if (patchModuleProject == compiledProject)
			compiledProject.files(compiledProject.the<SourceSetContainer>().matching { it.name.startsWith("main") }.map { it.output })
		else
			patchModuleProject.files(patchModuleProject.the<SourceSetContainer>()["main"].java.srcDirs)
	}

	override fun asArguments(): List<String> {
		val path = patch.get().filter { it.exists() }.asPath
		if (path.isEmpty()) {
			return emptyList()
		}
		return listOf("--patch-module", "$module=$path")
	}

	@Internal
	override fun getName() = "patch-module($module)"
}
