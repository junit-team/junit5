package junitbuild.java

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.internal.file.archive.ZipCopyAction
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.ExecOperations
import java.time.Instant
import javax.inject.Inject

abstract class UpdateJarAction @Inject constructor(private val operations: ExecOperations) : Action<Task> {

    abstract val javaLauncher: Property<JavaLauncher>

    abstract val args: ListProperty<String>

    abstract val date: Property<Instant>

    init {
        date.convention(Instant.ofEpochMilli(ZipCopyAction.CONSTANT_TIME_FOR_ZIP_ENTRIES))
    }

    override fun execute(t: Task) {
        operations.exec {
            executable = javaLauncher.get()
                .metadata.installationPath.file("bin/jar").asFile.absolutePath
            args = listOf(
                "--update",
                "--file", (t as AbstractArchiveTask).archiveFile.get().asFile.absolutePath,
                "--date=${date.get()}"
            ) + this@UpdateJarAction.args.get()
        }
    }
}
