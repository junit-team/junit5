package junitbuild.java

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.internal.file.archive.ZipCopyAction
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.ExecOperations
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

abstract class UpdateJarAction @Inject constructor(private val operations: ExecOperations): Action<Task> {

    companion object {
        // Since ZipCopyAction.CONSTANT_TIME_FOR_ZIP_ENTRIES is in the default time zone (see its Javadoc),
        // we're converting it to the same time in UTC here to make the jar reproducible regardless of the
        // build's time zone.
        private val CONSTANT_TIME_FOR_ZIP_ENTRIES = LocalDateTime.ofInstant(Instant.ofEpochMilli(ZipCopyAction.CONSTANT_TIME_FOR_ZIP_ENTRIES), ZoneId.systemDefault())
            .toInstant(ZoneOffset.UTC)
            .toString()
    }

    abstract val javaLauncher: Property<JavaLauncher>

    abstract val args: ListProperty<String>

    init {
        args.addAll(
            "--update",
            // Use a constant time to make the JAR reproducible.
            "--date=$CONSTANT_TIME_FOR_ZIP_ENTRIES",
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
