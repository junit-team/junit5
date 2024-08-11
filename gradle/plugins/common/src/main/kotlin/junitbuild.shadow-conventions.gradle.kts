import junitbuild.java.ModuleCompileOptions

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
}

sourceSets {
	main {
		compileClasspath += shadowedClasspath.get()
	}
	test {
		runtimeClasspath += shadowedClasspath.get()
	}
}

eclipse {
	classpath {
		plusConfigurations.add(shadowedClasspath.get())
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(shadowedClasspath.get())
	}
}

tasks {
	javadoc {
		classpath += shadowedClasspath.get()
	}
	checkstyleMain {
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
	test {
		dependsOn(shadowJar)
		// in order to run the test against the shadowJar
		classpath -= sourceSets.main.get().output
		classpath += files(shadowJar.map { it.archiveFile })
	}
	named<JavaCompile>("compileModule") {
		the<ModuleCompileOptions>().modulePath.from(shadowedClasspath.get())
	}
}
