import com.diffplug.gradle.spotless.SpotlessApply
import com.diffplug.gradle.spotless.SpotlessCheck
import com.diffplug.spotless.LineEnding

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
		targetExclude("**/build", "**/target")
		trimTrailingWhitespace()
		endWithNewline()
	}

	pluginManager.withPlugin("java") {

        val configDir = rootProject.layout.projectDirectory.dir("gradle/config/eclipse")
        val importOrderConfigFile = configDir.file("junit-eclipse.importorder")
		val javaFormatterConfigFile = configDir.file("junit-eclipse-formatter-settings.xml")

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

	// https://github.com/diffplug/spotless/issues/1644
	lineEndings = LineEnding.UNIX // or any other except GIT_ATTRIBUTES
}

tasks {
	withType<SpotlessApply>().configureEach {
		notCompatibleWithConfigurationCache("https://github.com/diffplug/spotless/issues/644")
	}
	withType<SpotlessCheck>().configureEach {
		notCompatibleWithConfigurationCache("https://github.com/diffplug/spotless/issues/644")
	}
}
