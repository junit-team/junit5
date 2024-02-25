package junitbuild.java

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.internal.file.archive.ZipCopyAction
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.ExecOperations
import java.time.Instant
import javax.inject.Inject

abstract class UpdateJarAction @Inject constructor(private val operations: ExecOperations): Action<Task> {

    abstract val javaLauncher: Property<JavaLauncher>

    abstract val args: ListProperty<String>

    init {
        args.addAll(
            "--update",
            "--date=${Instant.ofEpochMilli(ZipCopyAction.CONSTANT_TIME_FOR_ZIP_ENTRIES)}",
        )
    }

    override fun execute(t: Task) {
        operations.exec {
            executable = javaLauncher.get()
                .metadata.installationPath.file("bin/jar").asFile.absolutePath
            args = this@UpdateJarAction.args.get()
        }
    }
}
