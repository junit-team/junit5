import junitbuild.generator.GenerateJreRelatedSourceCode

plugins {
    java
}

val rootTargetDir = layout.buildDirectory.dir("generated/java")

val generateCode by tasks.registering

sourceSets.configureEach {

    val sourceSetName = name
    val sourceSetTargetDir = rootTargetDir.map { it.dir(sourceSetName) }

    val task =
        tasks.register(getTaskName("generateJreRelated", "SourceCode"), GenerateJreRelatedSourceCode::class) {
            templateResourceDir.convention("jre-templates/${project.name}/${sourceSetName}")
            targetDir.convention(sourceSetTargetDir)
        }

    java.srcDir(files(sourceSetTargetDir).builtBy(task))

    generateCode {
        dependsOn(task)
    }
}
