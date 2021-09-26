import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate

plugins {
    id("com.diffplug.spotless")
}

val license: License by rootProject.extra

spotless {

    format("misc") {
        target("*.gradle", "*.gradle.kts", "*.gitignore")
        indentWithTabs()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("documentation") {
        target("*.adoc", "*.md", "src/**/*.adoc", "src/**/*.md")
        trimTrailingWhitespace()
        endWithNewline()
    }

    pluginManager.withPlugin("java") {

        val importOrderConfigFile = rootProject.file("src/eclipse/junit-eclipse.importorder")
        val javaFormatterConfigFile = rootProject.file("src/eclipse/junit-eclipse-formatter-settings.xml")

        java {
            licenseHeaderFile(license.headerFile, "(package|import|open|module) ")
            importOrderFile(importOrderConfigFile)
            eclipse().configFile(javaFormatterConfigFile)
            if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_15)) {
                // Doesn't work with Java 15 text blocks, see https://github.com/diffplug/spotless/issues/713
                removeUnusedImports()
            }
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        kotlin {
            targetExclude("**/src/test/resources/**")
            val libs = project.extensions["libs"] as VersionCatalog
            ktlint(libs.findVersion("ktlint").get().requiredVersion)
            licenseHeaderFile(license.headerFile)
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

