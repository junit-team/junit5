package junitbuild.java

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class ExecJarAction @Inject constructor(private val operations: ExecOperations): Action<Task> {

    abstract val javaLauncher: Property<JavaLauncher>

    abstract val args: ListProperty<String>

    override fun execute(t: Task) {
        operations.exec {
            executable = javaLauncher.get()
                .metadata.installationPath.file("bin/jar").asFile.absolutePath
            args = this@ExecJarAction.args.get()
        }
    }
}
