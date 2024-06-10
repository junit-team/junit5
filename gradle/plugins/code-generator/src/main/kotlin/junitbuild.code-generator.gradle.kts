import junitbuild.generator.CodeGenerationConfiguration
import junitbuild.generator.GenerateJreRelatedSourceCode

plugins {
    java
}

val config = extensions.create("jreCodeGeneration", CodeGenerationConfiguration::class.java).apply {
    mainTargetDir.convention(layout.buildDirectory.dir("generated/java/main"))
}

val generateJreRelatedSourceCode by tasks.registering(GenerateJreRelatedSourceCode::class) {
    jreYaml.convention(config.jreYaml)
    mainTargetDir.convention(config.mainTargetDir)
}

sourceSets.main {
    java.srcDir(files(config.mainTargetDir).builtBy(generateJreRelatedSourceCode))
}
