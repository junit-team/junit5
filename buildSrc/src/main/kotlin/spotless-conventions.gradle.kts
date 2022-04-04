import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate

plugins {
	id("com.diffplug.spotless")
}

val license: License by rootProject.extra

spotless {

	format("misc") {
		target("*.gradle.kts", "buildSrc/**/*.gradle.kts", "*.gitignore")
		targetExclude("buildSrc/build/**")
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
			trimTrailingWhitespace()
			endWithNewline()
		}
	}

	pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
		kotlin {
			targetExclude("**/src/test/resources/**")
			ktlint(requiredVersionFromLibs("ktlint"))
			licenseHeaderFile(license.headerFile)
			trimTrailingWhitespace()
			endWithNewline()
		}
	}
}
