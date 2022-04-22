plugins {
	id("java-library-conventions")
}

val mavenizedProjects: List<Project> by rootProject.extra

listOf(9, 17).forEach { javaVersion ->
	val sourceSet = sourceSets.register("mainRelease${javaVersion}") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
		java {
			setSrcDirs(setOf("src/main/java${javaVersion}"))
		}
	}

	configurations.named(sourceSet.get().compileClasspathConfigurationName).configure {
		extendsFrom(configurations.compileClasspath.get())
	}

	tasks {

		named("allMainClasses").configure {
			dependsOn(sourceSet.get().classesTaskName)
		}

		named<JavaCompile>(sourceSet.get().compileJavaTaskName).configure {
			options.release.set(javaVersion)
		}

		named<Checkstyle>("checkstyle${sourceSet.name.capitalize()}").configure {
			configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
		}

		if (project in mavenizedProjects) {
			javadoc {
				source(sourceSet.get().allJava)
			}
			named<Jar>("sourcesJar").configure {
				from(sourceSet.get().allSource)
			}
		}
	}
}
