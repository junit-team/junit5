package junitbuild.exec

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.process.ExecOperations
import java.nio.file.Files
import javax.inject.Inject

@CacheableTask
abstract class CaptureJavaExecOutput @Inject constructor(private val execOperations: ExecOperations) : DefaultTask() {

    @get:Classpath
    abstract val classpath: ConfigurableFileCollection

    @get:Input
    abstract val mainClass: Property<String>

    @get:Input
    abstract val args: ListProperty<String>

    @get:Nested
    val jvmArgumentProviders = mutableListOf<CommandLineArgumentProvider>()

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val outputFile = outputFile.get().asFile.toPath()
        Files.newOutputStream(outputFile).use { out ->
            execOperations.javaexec {
                classpath = this@CaptureJavaExecOutput.classpath
                mainClass.set(this@CaptureJavaExecOutput.mainClass)
                args = this@CaptureJavaExecOutput.args.get()
                jvmArgumentProviders.addAll(this@CaptureJavaExecOutput.jvmArgumentProviders)
                standardOutput = out
            }
        }
    }
}
