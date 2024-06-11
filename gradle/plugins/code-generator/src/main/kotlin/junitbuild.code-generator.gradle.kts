import junitbuild.generator.CodeGenerationConfiguration
import junitbuild.generator.GenerateJreRelatedSourceCode

plugins {
    java
}

val config = extensions.create("jreCodeGeneration", CodeGenerationConfiguration::class.java).apply {
    targetDir.convention(layout.buildDirectory.dir("generated/java"))
}

val generateCode by tasks.registering

sourceSets.configureEach {

    val sourceSetName = name
    val sourceSetTargetDir = config.targetDir.map { it.dir(sourceSetName) }

    val task =
        tasks.register(getTaskName("generate", "JreRelatedSourceCode"), GenerateJreRelatedSourceCode::class) {
            jreYaml.convention(config.jreYaml)
            targetDir.convention(sourceSetTargetDir)
            templateResourceDir.convention("jre-templates/${sourceSetName}")
        }

    java.srcDir(files(sourceSetTargetDir).builtBy(task))

    generateCode {
        dependsOn(task)
    }
}
