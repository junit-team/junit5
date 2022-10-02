package org.junit.gradle.exec

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.process.ExecOperations
import trackOperationSystemAsInput
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class RunConsoleLauncher @Inject constructor(private val execOperations: ExecOperations): DefaultTask() {

    @get:Classpath
    abstract val runtimeClasspath: ConfigurableFileCollection

    @get:Input
    abstract val args: ListProperty<String>

    @get:OutputDirectory
    abstract val reportsDir: DirectoryProperty

    @get:Internal
    abstract val debugging: Property<Boolean>

    @get:Internal
    abstract val hideOutput: Property<Boolean>

    init {
        runtimeClasspath.from(project.the<SourceSetContainer>()["test"].runtimeClasspath)
        reportsDir.convention(project.layout.buildDirectory.dir("test-results"))

        debugging.convention(
            project.providers.gradleProperty("consoleLauncherTestDebug")
            .map { it != "false" }
            .orElse(false)
        )
        outputs.cacheIf { !debugging.get() }
        outputs.upToDateWhen { !debugging.get() }

        hideOutput.convention(debugging.map { !it })

        trackOperationSystemAsInput()
    }

    @TaskAction
    fun execute() {
        val output = ByteArrayOutputStream()
        val result = execOperations.javaexec {
            classpath = runtimeClasspath
            mainClass.set("org.junit.platform.console.ConsoleLauncher")
            args("--scan-classpath")
            args("--config=junit.platform.reporting.open.xml.enabled=true")
            args(this@RunConsoleLauncher.args.get())
            argumentProviders += CommandLineArgumentProvider {
                listOf(
                    "--reports-dir=${reportsDir.get()}",
                    "--config=junit.platform.reporting.output.dir=${reportsDir.get()}"

                )
            }
            systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
            debug = debugging.get()
            if (hideOutput.get()) {
                standardOutput = output
                errorOutput = output
            }
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0 && hideOutput.get()) {
            System.out.write(output.toByteArray())
            System.out.flush()
        }
        result.rethrowFailure().assertNormalExitValue()
    }
}
