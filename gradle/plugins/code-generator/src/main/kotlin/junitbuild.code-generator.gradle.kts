import junitbuild.generator.CodeGenerationConfiguration
import junitbuild.generator.GenerateJreRelatedSourceCode

plugins {
    java
}

val config = extensions.create("jreCodeGeneration", CodeGenerationConfiguration::class.java)

config.mainTargetDir.convention(layout.buildDirectory.dir("generated/java"))
sourceSets.main.get().java.srcDir(config.mainTargetDir)

tasks {
    val generateJreRelatedSourceCode by registering(GenerateJreRelatedSourceCode::class) {
        jreYaml.convention(config.jreYaml)
        mainTargetDir.convention(config.mainTargetDir)
    }
    compileJava {
        dependsOn(generateJreRelatedSourceCode)
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        named("compileKotlin") {
            dependsOn(generateJreRelatedSourceCode)
        }
    }
}
