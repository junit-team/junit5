package junitbuild.exec

import org.apache.tools.ant.types.Commandline
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.process.ExecOperations
import trackOperationSystemAsInput
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

@CacheableTask
abstract class RunConsoleLauncher @Inject constructor(private val execOperations: ExecOperations) : DefaultTask() {

    @get:Classpath
    abstract val runtimeClasspath: ConfigurableFileCollection

    @get:Input
    abstract val args: ListProperty<String>

    @get:Nested
    abstract val argumentProviders: ListProperty<CommandLineArgumentProvider>

    @get:Input
    abstract val commandLineArgs: ListProperty<String>

    @get:Nested
    abstract val javaLauncher: Property<JavaLauncher>

    @get:Internal
    abstract val debugging: Property<Boolean>

    @get:Internal
    abstract val hideOutput: Property<Boolean>

    init {
        runtimeClasspath.from(project.the<SourceSetContainer>()["test"].runtimeClasspath)
        javaLauncher.set(project.the<JavaToolchainService>().launcherFor(project.the<JavaPluginExtension>().toolchain))

        debugging.convention(false)
        commandLineArgs.convention(emptyList())
        outputs.cacheIf { !debugging.get() }
        outputs.upToDateWhen { !debugging.get() }

        hideOutput.convention(debugging.map { !it })

        trackOperationSystemAsInput()
    }

    @TaskAction
    fun execute() {
        val output = ByteArrayOutputStream()
        val result = execOperations.javaexec {
            executable = javaLauncher.get().executablePath.asFile.absolutePath
            classpath = runtimeClasspath
            mainClass.set("org.junit.platform.console.ConsoleLauncher")
            args(this@RunConsoleLauncher.args.get())
            args(this@RunConsoleLauncher.commandLineArgs.get())
            argumentProviders.addAll(this@RunConsoleLauncher.argumentProviders.get())
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

    @Suppress("unused")
    @Option(option = "args", description = "Additional command line arguments for the console launcher")
    fun setCliArgs(args: String) {
        commandLineArgs.set(Commandline.translateCommandline(args).toList())
    }

    @Suppress("unused")
    @Option(
        option = "debug-jvm",
        description = "Enable debugging. The process is started suspended and listening on port 5005."
    )
    fun setDebug(enabled: Boolean) {
        debugging.set(enabled)
    }

}
