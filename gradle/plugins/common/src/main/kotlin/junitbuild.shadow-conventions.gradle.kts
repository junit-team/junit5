import gradle.kotlin.dsl.accessors._081daccbbc3bc6bb53248544394ccd4e.shadowJar
import gradle.kotlin.dsl.accessors._081daccbbc3bc6bb53248544394ccd4e.shadowRuntimeElements
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
	id("junitbuild.java-library-conventions")
	id("com.gradleup.shadow")
}

val shadowed = configurations.dependencyScope("shadowed")
val shadowedClasspath = configurations.resolvable("shadowedClasspath") {
	extendsFrom(shadowed.get())
}

configurations {
	listOf(apiElements, runtimeElements).forEach {
		it.configure {
			outgoing {
				artifacts.clear()
				artifact(tasks.shadowJar) {
					classifier = ""
				}
			}
		}
	}
	compileClasspath {
		extendsFrom(shadowed.get())
	}
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations.shadowRuntimeElements.get()) { skip() }

tasks {
	javadoc {
		classpath += shadowedClasspath.get()
	}
	shadowJar {
		configurations = listOf(shadowedClasspath.get())
		exclude("META-INF/maven/**")
		excludes.remove("module-info.class")
		archiveClassifier = ""
	}
	jar {
		dependsOn(shadowJar)
		enabled = false
	}
	named<Jar>("codeCoverageClassesJar") {
		from(shadowJar.map { zipTree(it.archiveFile) })
		exclude("**/shadow/**")
	}
}
