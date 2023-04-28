package junitbuild.exec

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.process.CommandLineArgumentProvider

class ClasspathSystemPropertyProvider(private val propertyName: String, @get:Classpath val files: FileCollection) : CommandLineArgumentProvider {
    override fun asArguments() = listOf("-D$propertyName=${files.asPath}")
}
