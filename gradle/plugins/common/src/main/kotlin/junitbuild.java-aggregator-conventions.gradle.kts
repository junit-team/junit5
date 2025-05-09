import org.gradle.kotlin.dsl.kotlin

plugins {
	id("junitbuild.java-library-conventions")
}

tasks.javadoc {
	// Since this JAR contains no classes, running Javadoc fails with:
	// "No public or protected classes found to document"
	enabled = false
}
