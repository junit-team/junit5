plugins {
	id("java-library-conventions")
	id("com.github.johnrengelman.shadow")
}

val shadowed by configurations.creating

configurations.forEach { configuration ->
	configuration.outgoing.apply {
		val removed = artifacts.removeIf { it.classifier.isNullOrEmpty() }
		if (removed) {
			artifact(tasks.shadowJar) {
				classifier = ""
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
		archiveClassifier.set("")
	}
	jar {
		dependsOn(shadowJar)
		enabled = false
	}
	test {
		dependsOn(shadowJar)
		// in order to run the test against the shadowJar
		classpath -= sourceSets.main.get().output
		classpath += files(shadowJar.map { it.archiveFile })
	}
}
