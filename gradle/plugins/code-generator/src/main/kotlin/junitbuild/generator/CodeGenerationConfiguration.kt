package junitbuild.generator

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

abstract class CodeGenerationConfiguration {

    abstract val jreYaml: RegularFileProperty

    abstract val targetDir: DirectoryProperty

}
