import junitbuild.java.ModuleCompileOptions

plugins {
	id("junitbuild.java-library-conventions")
	id("com.github.johnrengelman.shadow")
}

val shadowed by configurations.creatingResolvable

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
		compileClasspath += shadowed
	}
	test {
		runtimeClasspath += shadowed
	}
}

eclipse {
	classpath {
		plusConfigurations.add(shadowed)
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(shadowed)
	}
}

tasks {
	javadoc {
		classpath += shadowed
	}
	checkstyleMain {
		classpath += shadowed
	}
	shadowJar {
		configurations = listOf(shadowed)
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
        the<ModuleCompileOptions>().modulePath.from(shadowed)
    }
}
