import junitbuild.generator.GenerateJreRelatedSourceCode

plugins {
    java
}

val rootTargetDir = layout.buildDirectory.dir("generated/sources/jte")

val generateCode by tasks.registering

sourceSets {
    val templates by registering
    dependencies {
        add(templates.get().compileOnlyConfigurationName, dependencyFromLibs("jte"))
        add(templates.get().compileOnlyConfigurationName, "junitbuild.base:code-generator-model")
    }
    named { it != templates.name }.configureEach {

        val sourceSetName = name
        val sourceSetTargetDir = rootTargetDir.map { it.dir(sourceSetName) }

        val task =
            tasks.register(getTaskName("generateJreRelated", "SourceCode"), GenerateJreRelatedSourceCode::class) {
                templateDir.convention(layout.dir(templates.map { it.resources.srcDirs.single().resolve(sourceSetName) }))
                targetDir.convention(sourceSetTargetDir)
                licenseHeaderFile.convention(rootProject.layout.projectDirectory.file("gradle/config/spotless/eclipse-public-license-2.0.java"))
            }

        java.srcDir(files(sourceSetTargetDir).builtBy(task))

        generateCode {
            dependsOn(task)
        }
    }
}
