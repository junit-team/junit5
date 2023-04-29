package junitbuild.exec

import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
abstract class GenerateStandaloneConsoleLauncherShadowedArtifactsFile @Inject constructor(
    private val fileSystem: FileSystemOperations,
    private val archives: ArchiveOperations
) : DefaultTask() {

    @get:Classpath
    abstract val inputJar: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        fileSystem.copy {
            from(archives.zipTree(inputJar)) {
                include("META-INF/shadowed-artifacts")
                includeEmptyDirs = false
                eachFile {
                    relativePath = RelativePath(true, outputFile.get().asFile.name)
                }
                filter { line -> "- `${line}`" }
            }
            into(outputFile.get().asFile.parentFile)
        }
    }
}
